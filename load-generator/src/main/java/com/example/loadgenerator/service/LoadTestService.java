package com.example.loadgenerator.service;

import com.example.loadgenerator.config.LoadGeneratorConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class LoadTestService {
    private final RestTemplate restTemplate;
    private final LoadGeneratorConfig config;
    private final Random random = new Random();

    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong successfulRequests = new AtomicLong(0);
    private final AtomicLong failedRequests = new AtomicLong(0);
    private final AtomicLong totalResponseTime = new AtomicLong(0);

    public LoadTestService(RestTemplateBuilder restTemplateBuilder, LoadGeneratorConfig config) {
        this.config = config;
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(10))
                .build();
    }

    public void executeRandomRequest() {
        long clientId = generateRandomClientId();
        long startTime = System.currentTimeMillis();

        try {
            String url = config.getTargetUrl() + "/api/clients/" + clientId + "/profile";
            restTemplate.getForObject(url, String.class);

            long responseTime = System.currentTimeMillis() - startTime;
            successfulRequests.incrementAndGet();
            totalResponseTime.addAndGet(responseTime);

            log.debug("Успешный запрос для clientId={}, время={}мс", clientId, responseTime);

        } catch (HttpClientErrorException.NotFound e) {
            log.debug("Клиент не найден: clientId={}", clientId);
            successfulRequests.incrementAndGet(); // Считаем как успешный (кэш все равно работает)
        } catch (ResourceAccessException e) {
            log.error("Ошибка доступа к сервису: {}", e.getMessage());
            failedRequests.incrementAndGet();
        } catch (Exception e) {
            log.error("Ошибка при запросе clientId={}: {}", clientId, e.getMessage());
            failedRequests.incrementAndGet();
        } finally {
            totalRequests.incrementAndGet();
        }
    }

    private long generateRandomClientId() {
        return config.getMinClientId() + random.nextInt(
                config.getMaxClientId() - config.getMinClientId() + 1
        );
    }

    public void printStatistics() {
        long total = totalRequests.get();
        long successful = successfulRequests.get();
        long failed = failedRequests.get();
        long totalTime = totalResponseTime.get();

        log.info("=== СТАТИСТИКА НАГРУЗКИ ===");
        log.info("Всего запросов: {}", total);
        log.info("Успешных: {} ({}%)", successful,
                total > 0 ? String.format("%.2f", (successful * 100.0 / total)) : "0");
        log.info("Ошибок: {} ({}%)", failed,
                total > 0 ? String.format("%.2f", (failed * 100.0 / total)) : "0");

        if (successful > 0) {
            double avgTime = (double) totalTime / successful;
            log.info("Среднее время ответа: {:.2f} мс", avgTime);
        }

        log.info("Текущий RPS: {}", config.getRequestsPerSecond());
        log.info("===========================");
    }

    public void resetStatistics() {
        totalRequests.set(0);
        successfulRequests.set(0);
        failedRequests.set(0);
        totalResponseTime.set(0);
        log.info("Статистика сброшена");
    }

    public boolean isTargetServiceAvailable() {
        try {
            restTemplate.getForObject(config.getTargetUrl() + "/actuator/health", String.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}