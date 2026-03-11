package com.example.app;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CacheConfigurationPrinter implements CommandLineRunner {

    @Autowired
    private CacheManager cacheManager;

    @Override
    public void run(String... args) {
        log.info("=== КОНФИГУРАЦИЯ КЭША ===");
        log.info("CacheManager class: {}", cacheManager.getClass().getName());

        if (cacheManager instanceof CaffeineCacheManager) {
            CaffeineCacheManager cm = (CaffeineCacheManager) cacheManager;
            log.info("Тип: CaffeineCacheManager");
            log.info("Cache names: {}", cm.getCacheNames());

            // Попытка получить спецификацию Caffeine
            try {
                java.lang.reflect.Field field = CaffeineCacheManager.class.getDeclaredField("cacheSpecification");
                field.setAccessible(true);
                String spec = (String) field.get(cm);
                log.info("Caffeine spec: {}", spec != null ? spec : "не задана (будет использована дефолтная)");
            } catch (Exception e) {
                log.info("Caffeine spec: не удалось прочитать");
            }

            try {
                java.lang.reflect.Field field = CaffeineCacheManager.class.getDeclaredField("allowNullValues");
                field.setAccessible(true);
                boolean allowNull = field.getBoolean(cm);
                log.info("Allow null values: {}", allowNull);
            } catch (Exception e) {
                log.info("Allow null values: неизвестно");
            }

            log.info("Caffeine успешно используется!");

        } else if (cacheManager instanceof ConcurrentMapCacheManager) {
            ConcurrentMapCacheManager cm = (ConcurrentMapCacheManager) cacheManager;
            log.info("Тип: ConcurrentMapCacheManager (default)");
            log.info("Cache names: {}", cm.getCacheNames());
            log.info("Allow null values: {}", cm.isAllowNullValues());
            log.info("Store by value: {}", cm.isStoreByValue());

            // Информация о режиме (статический/динамический)
            try {
                java.lang.reflect.Field field = ConcurrentMapCacheManager.class.getDeclaredField("dynamic");
                field.setAccessible(true);
                boolean isDynamic = field.getBoolean(cm);
                log.info("Dynamic mode: {} ({} создание кэшей)",
                        isDynamic,
                        isDynamic ? "динамическое" : "статическое");
            } catch (Exception e) {
                log.info("Dynamic mode: неизвестно");
            }
        }

        log.info("===========================");
    }
}