package com.example.app.controller.stats;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.exception.MemcachedException;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Profile("caffeine")
@RestController
@RequestMapping("/api/cache/memcached")
@RequiredArgsConstructor
@Slf4j
public class MemcachedStatsController {

    private final CacheManager cacheManager;
    private final MemcachedClient memcachedClient;

    @GetMapping("/stats")
    public Map<String, Object> getMemcachedStats() {
        Map<String, Object> stats = new HashMap<>();

        try {
            // Memcached не предоставляет нативной статистики через протокол,
            // но можно получить некоторую информацию

            stats.put("cache_names", cacheManager.getCacheNames());
            stats.put("servers", memcachedClient.getAvailableServers());

            // Проверка доступности
            boolean isConnected = !memcachedClient.getAvailableServers().isEmpty();
            stats.put("connected", isConnected);

            if (isConnected) {
                // Тестовый запрос для проверки
                memcachedClient.set("health-check", 10, "ok");
                String healthValue = memcachedClient.get("health-check");
                stats.put("health_check", healthValue);
            }

        } catch (Exception e) {
            log.error("Ошибка при получении статистики Memcached", e);
            stats.put("error", e.getMessage());
        }

        return stats;
    }

    @GetMapping("/flush")
    public Map<String, String> flushAll() {
        try {
            memcachedClient.flushAll();
            return Map.of("message", "Все кэши Memcached очищены");
        } catch (TimeoutException | InterruptedException | MemcachedException e) {
            log.error("Ошибка при очистке Memcached", e);
            return Map.of("error", e.getMessage());
        }
    }
}