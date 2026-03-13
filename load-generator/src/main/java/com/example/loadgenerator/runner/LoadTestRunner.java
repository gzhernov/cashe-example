package com.example.loadgenerator.runner;

import com.example.loadgenerator.config.LoadGeneratorConfig;
import com.example.loadgenerator.service.LoadTestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
public class LoadTestRunner implements CommandLineRunner {
    private final LoadTestService loadTestService;
    private final LoadGeneratorConfig config;
    private final ThreadPoolTaskScheduler taskScheduler;

    private ScheduledFuture<?> loadTask;
    private ScheduledFuture<?> statsTask;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private Instant startTime;

    public LoadTestRunner(LoadTestService loadTestService, LoadGeneratorConfig config) {
        this.loadTestService = loadTestService;
        this.config = config;

        this.taskScheduler = new ThreadPoolTaskScheduler();
        this.taskScheduler.setPoolSize(config.getThreads()+7);
        this.taskScheduler.setThreadNamePrefix("load-generator-");
        this.taskScheduler.initialize();
    }

    @Override
    public void run(String... args) {
        if (!config.isEnabled()) {
            log.info("Генератор нагрузки отключен в конфигурации");
            return;
        }

        log.info("=== ЗАПУСК ГЕНЕРАТОРА НАГРУЗКИ ===");
        log.info("Целевой URL: {}", config.getTargetUrl());
        log.info("Потоков: {}", config.getThreads());
        log.info("Запросов в секунду: {}", config.getRequestsPerSecond());
        log.info("Диапазон clientId: {}-{}", config.getMinClientId(), config.getMaxClientId());

        // Проверяем доступность сервиса
        if (!loadTestService.isTargetServiceAvailable()) {
            log.error("Целевой сервис недоступен по адресу: {}. Завершение работы.",
                    config.getTargetUrl());
            return;
        }
        log.info("Целевой сервис доступен, начинаем нагрузочное тестирование...");

        startTime = Instant.now();
        running.set(true);

        // Запускаем задачи нагрузки
        startLoadTest();

        // Запускаем задачу для вывода статистики каждые 10 секунд
        statsTask = taskScheduler.scheduleAtFixedRate(
                this::printStatistics,
                Duration.ofSeconds(60)
        );

        // Добавляем задачу для проверки длительности выполнения
        if (config.getRunDurationMinutes() > 0) {
            taskScheduler.schedule(this::stopLoadTest,
                    Instant.now().plus(config.getRunDurationMinutes(), ChronoUnit.MINUTES));
        }

        // Добавляем shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(this::stopLoadTest));
    }

    private void startLoadTest() {
        // Рассчитываем задержку между запросами (в миллисекундах)
        long delayMs = 1000 / config.getRequestsPerSecond();

        // Запускаем несколько потоков для достижения нужного RPS
        for (int i = 0; i < config.getThreads(); i++) {
            taskScheduler.scheduleAtFixedRate(
                    this::executeLoadTask,
                    Instant.now().plusMillis(i * delayMs / config.getThreads()),
                    Duration.ofMillis(delayMs)
            );
        }

        log.info("Задачи нагрузки запущены");
    }

    private void executeLoadTask() {
        if (!running.get()) {
            return;
        }
        loadTestService.executeRandomRequest();
    }

    private void printStatistics() {
        if (!running.get()) {
            return;
        }

        long elapsedMinutes = Duration.between(startTime, Instant.now()).toMinutes();
        log.info("--- Прошло минут: {} ---", elapsedMinutes);
        loadTestService.printStatistics();
    }

    private void stopLoadTest() {
        if (!running.get()) {
            return;
        }

        log.info("=== ОСТАНОВКА ГЕНЕРАТОРА НАГРУЗКИ ===");
        running.set(false);

        if (statsTask != null) {
            statsTask.cancel(false);
        }

        // Финальная статистика
        loadTestService.printStatistics();

        long elapsedMinutes = Duration.between(startTime, Instant.now()).toMinutes();
        log.info("Тест выполнялся {} минут", elapsedMinutes);

        taskScheduler.shutdown();
        try {
            if (!taskScheduler.getScheduledThreadPoolExecutor().awaitTermination(5, TimeUnit.SECONDS)) {
                taskScheduler.getScheduledThreadPoolExecutor().shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}