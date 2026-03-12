package com.example.app.controller;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisServerCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@RestController
@RequestMapping("/api/cache")
@RequiredArgsConstructor
@Slf4j
public class RedisCacheStatisticsController {

    private final RedisTemplate<String, Object> redisTemplate;
    private final MeterRegistry meterRegistry;

    @GetMapping("/stats")
    public Map<String, Object> getRedisStats() {
        Map<String, Object> stats = new HashMap<>();

        try {
            // Информация о Redis сервере
            Properties info = redisTemplate.execute((RedisCallback<Properties>)
                    connection -> connection.info("stats"));

            Properties serverInfo = redisTemplate.execute((RedisCallback<Properties>)
                    connection -> connection.info("server"));

            Properties memoryInfo = redisTemplate.execute((RedisCallback<Properties>)
                    connection -> connection.info("memory"));

            // Сбор статистики
            Map<String, Object> redisInfo = new HashMap<>();

            if (info != null) {
                redisInfo.put("total_connections_received", info.getProperty("total_connections_received"));
                redisInfo.put("total_commands_processed", info.getProperty("total_commands_processed"));
                redisInfo.put("keyspace_hits", info.getProperty("keyspace_hits"));
                redisInfo.put("keyspace_misses", info.getProperty("keyspace_misses"));
                redisInfo.put("hit_rate", calculateHitRate(info));
            }

            if (memoryInfo != null) {
                redisInfo.put("used_memory", memoryInfo.getProperty("used_memory"));
                redisInfo.put("used_memory_human", memoryInfo.getProperty("used_memory_human"));
                redisInfo.put("maxmemory", memoryInfo.getProperty("maxmemory"));
                redisInfo.put("maxmemory_policy", memoryInfo.getProperty("maxmemory_policy"));
            }

            if (serverInfo != null) {
                redisInfo.put("redis_version", serverInfo.getProperty("redis_version"));
                redisInfo.put("uptime_in_seconds", serverInfo.getProperty("uptime_in_seconds"));
            }

            // Получаем размеры кэшей
            Map<String, Long> cacheSizes = new HashMap<>();
            for (String cacheName : getCacheNames()) {
                Long size = redisTemplate.opsForSet().size(cacheName + ":keys");
                cacheSizes.put(cacheName, size != null ? size : 0);
            }

            stats.put("redis", redisInfo);
            stats.put("cache_sizes", cacheSizes);

            // Добавляем метрики в Micrometer
            recordMetrics(info);

        } catch (Exception e) {
            log.error("Ошибка при получении статистики Redis", e);
            stats.put("error", e.getMessage());
        }

        return stats;
    }

    @GetMapping("/keys")
    public Map<String, Object> getAllCacheKeys() {
        Map<String, Object> result = new HashMap<>();

        for (String cacheName : getCacheNames()) {
            String pattern = cacheName + ":*";
            result.put(cacheName, redisTemplate.keys(pattern));
        }

        return result;
    }

    private String[] getCacheNames() {
        return new String[]{
                "clientProfiles",
                "accountInfo",
                "activeCampaigns",
                "audienceSegments",
                "performanceReports",
                "billingInfo",
                "targetingSettings",
                "notificationSettings",
                "topCreatives",
                "dailyAnalytics"
        };
    }

    private double calculateHitRate(Properties info) {
        String hits = info.getProperty("keyspace_hits", "0");
        String misses = info.getProperty("keyspace_misses", "0");

        long hitsLong = Long.parseLong(hits);
        long missesLong = Long.parseLong(misses);
        long total = hitsLong + missesLong;

        return total > 0 ? (double) hitsLong / total : 0.0;
    }

    private void recordMetrics(Properties info) {
        if (info != null) {
            meterRegistry.gauge("redis.keyspace.hits",
                    Long.parseLong(info.getProperty("keyspace_hits", "0")));
            meterRegistry.gauge("redis.keyspace.misses",
                    Long.parseLong(info.getProperty("keyspace_misses", "0")));
        }
    }
}