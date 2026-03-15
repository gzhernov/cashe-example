package com.example.app.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisHealthChecker {

    private final RedisTemplate<String, Object> redisTemplate;
//    private final CacheManager cacheManager;

    private final Map<String, Boolean> cacheStatus = new ConcurrentHashMap<>();
    private volatile boolean globalRedisStatus = true;

//    @Value("${spring.data.redis.health-check.interval:5000}")
//    private final long healthCheckInterval; // Добавьте это поле

    @Scheduled(fixedDelayString = "${spring.data.redis.health-check.interval:5000}") // Проверка каждые 30 секунд
    public void checkRedisHealth() {
        boolean wasAvailable = globalRedisStatus;
        try {
            String pong = redisTemplate.getConnectionFactory().getConnection().ping();
            boolean isAvailable = "PONG".equals(pong);

            globalRedisStatus = isAvailable;

            if (!wasAvailable && isAvailable) {
                log.info("✅ Redis восстановлен! Возобновляем кэширование.");
            } else if (wasAvailable && !isAvailable) {
                log.error("❌ Redis потерян! Переключаемся на прямой режим.");
            }

        } catch (Exception e) {
            globalRedisStatus = false;
            if (wasAvailable) {
                log.error("❌ Redis потерян! Ошибка: {}", e.getMessage());
            }
        }
    }

//    @Override
//    public Health health() {
//        if (globalRedisStatus) {
//            return Health.up()
//                    .withDetail("status", "connected")
//                    .withDetail("caches", cacheManager.getCacheNames())
//                    .build();
//        } else {
//            return Health.down()
//                    .withDetail("status", "disconnected")
//                    .withDetail("fallback", "Using direct backoffice calls")
//                    .build();
//        }
//    }

    public void setGlobalRedisStatus(boolean globalRedisStatus) {
        this.globalRedisStatus = globalRedisStatus;
    }

    public boolean isRedisAvailable() {
        return globalRedisStatus;
    }
}