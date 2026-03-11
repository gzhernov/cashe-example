import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class SchedulerApplication {

    public static void main(String[] args) {
        // Создаем и настраиваем ThreadPoolTaskScheduler
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("MyScheduler-");
        scheduler.initialize();

        System.out.println("Программа запущена. Текущее время: " + Instant.now());

        // Планируем задачу через 1 минуту
        scheduler.schedule(() -> {
            System.out.println("Hello! Текущее время: " + Instant.now());
        }, Instant.now().plus(1, ChronoUnit.MINUTES));

        System.out.println("Задача запланирована на выполнение через 1 минуту");

        // Даем программе время на выполнение задачи
        try {
            Thread.sleep(70000); // Ждем 70 секунд (немного больше минуты)
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Останавливаем планировщик
        scheduler.shutdown();
        System.out.println("Программа завершена");
    }
}