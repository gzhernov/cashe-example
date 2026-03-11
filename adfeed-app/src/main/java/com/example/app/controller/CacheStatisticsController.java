package com.example.app.controller;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cache")
public class CacheStatisticsController {

    private final CacheManager cacheManager;

    public CacheStatisticsController(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @GetMapping("/stats")
    public Map<String, Object> getDetailedCacheStats() {
        Map<String, Object> stats = new HashMap<>();

        CaffeineCache caffeineCache = (CaffeineCache) cacheManager.getCache("clientProfiles");
        if (caffeineCache != null) {
            Cache<Object, Object> nativeCache = caffeineCache.getNativeCache();
            CacheStats cacheStats = nativeCache.stats();

            Map<String, Object> cacheDetails = new HashMap<>();
            // Основные метрики
            cacheDetails.put("hitCount", cacheStats.hitCount());
            cacheDetails.put("missCount", cacheStats.missCount());
            cacheDetails.put("hitRate", cacheStats.hitRate());
            cacheDetails.put("missRate", cacheStats.missRate());
            cacheDetails.put("evictionCount", cacheStats.evictionCount());
            cacheDetails.put("evictionWeight", cacheStats.evictionWeight());

            // Размер кэша
            cacheDetails.put("estimatedSize", nativeCache.estimatedSize());

//            cacheDetails.put("exactSize", nativeCache.asMap().size()); // точный размер

            // Полиморфная статистика размера
            cacheDetails.put("maximumSize", getMaximumSize(nativeCache));

            // Расширенная статистика
            cacheDetails.put("totalLoadTime", cacheStats.totalLoadTime());
            cacheDetails.put("averageLoadPenalty", cacheStats.averageLoadPenalty());
            cacheDetails.put("loadSuccessCount", cacheStats.loadSuccessCount());
            cacheDetails.put("loadFailureCount", cacheStats.loadFailureCount());
            cacheDetails.put("loadFailureRate", cacheStats.loadFailureRate());



            stats.put("clientProfiles", cacheDetails);
        }

        return stats;
    }


    // Метод для получения maximumSize через рефлексию
    private Long getMaximumSize(Cache<Object, Object> nativeCache) {
        try {
            // Пытаемся получить политику вытеснения
//            com.github.benmanes.caffeine.cache.Policy<?, ?> policy = nativeCache.policy();

            // Пытаемся получить эвикшен через рефлексию
            java.lang.reflect.Field evictionField = nativeCache.getClass().getDeclaredField("eviction");
            evictionField.setAccessible(true);
            Object eviction = evictionField.get(nativeCache);

            java.lang.reflect.Method maxSizeMethod = eviction.getClass().getMethod("getMaximum");
            return (Long) maxSizeMethod.invoke(eviction);
        } catch (Exception e) {
            return null;
        }
    }
}