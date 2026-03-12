package com.example.app;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CacheConfigurationPrinter implements CommandLineRunner {

    @Autowired
    private CacheManager cacheManager;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void run(String... args) {
        log.info("=== КОНФИГУРАЦИЯ КЭША (REDIS) ===");
        log.info("CacheManager class: {}", cacheManager.getClass().getName());

        if (cacheManager instanceof RedisCacheManager) {
            RedisCacheManager cm = (RedisCacheManager) cacheManager;
            log.info("Тип: RedisCacheManager");
            log.info("Cache names: {}", cm.getCacheNames());

            try {
                // Получаем информацию о Redis сервере
                if (redisTemplate != null) {
                    redisTemplate.execute((RedisCallback<Object>) connection -> {
                        log.info("Redis server version: {}", connection.info("server").getProperty("redis_version"));
                        log.info("Redis connected to: {}", connection.getClientName());
                        return null;
                    });
                }
            } catch (Exception e) {
                log.warn("Не удалось получить информацию о Redis: {}", e.getMessage());
            }

            log.info("Redis успешно используется как провайдер кэша!");
        }

        log.info("===========================");
    }
}