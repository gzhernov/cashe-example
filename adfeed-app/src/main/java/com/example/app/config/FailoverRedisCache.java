package com.example.app.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.cache.RedisCache;

import java.util.concurrent.Callable;

@Slf4j
public class FailoverRedisCache implements Cache {

    private final RedisHealthChecker redisHealthChecker;
    private final RedisCache redisCache;
    private final String cacheName;

    public FailoverRedisCache(RedisHealthChecker redisHealthChecker, RedisCache redisCache) {
        this.redisHealthChecker = redisHealthChecker;
        this.redisCache = redisCache;
        this.cacheName = redisCache.getName();
    }

    @Override
    public String getName() {
        return cacheName;
    }

    @Override
    public Object getNativeCache() {
        return redisCache.getNativeCache();
    }

    @Override
    public ValueWrapper get(Object key) {
        if (!redisHealthChecker.isRedisAvailable()) {
            log.debug("Redis недоступен, пропускаем кэширование для ключа: {}", key);
            return null;
        }

        try {
            ValueWrapper value = redisCache.get(key);
            return value;
        } catch (RedisConnectionFailureException | QueryTimeoutException e) {
            handleRedisFailure(key, e);
            return null;
        } catch (Exception e) {
            log.error("Неожиданная ошибка Redis для ключа {}: {}", key, e.getMessage());
            return null;
        }
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        if (!redisHealthChecker.isRedisAvailable()) {
            log.debug("Redis недоступен, пропускаем кэширование для ключа: {}", key);
            return null;
        }

        try {
            T value = redisCache.get(key, type);
            return value;
        } catch (RedisConnectionFailureException | QueryTimeoutException e) {
            handleRedisFailure(key, e);
            return null;
        } catch (Exception e) {
            log.error("Неожиданная ошибка Redis для ключа {}: {}", key, e.getMessage());
            return null;
        }
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        if (!redisHealthChecker.isRedisAvailable()) {
            log.debug("Redis недоступен, вызываем valueLoader напрямую для ключа: {}", key);
            try {
                return valueLoader.call();
            } catch (Exception e) {
                throw new ValueRetrievalException(key, valueLoader, e);
            }
        }

        try {
            T value = redisCache.get(key, () -> {
                try {
                    return valueLoader.call();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            return value;
        } catch (RedisConnectionFailureException | QueryTimeoutException e) {
            handleRedisFailure(key, e);
            try {
                return valueLoader.call();
            } catch (Exception ex) {
                throw new ValueRetrievalException(key, valueLoader, ex);
            }
        } catch (Exception e) {
            log.error("Неожиданная ошибка Redis для ключа {}: {}", key, e.getMessage());
            try {
                return valueLoader.call();
            } catch (Exception ex) {
                throw new ValueRetrievalException(key, valueLoader, ex);
            }
        }
    }

    @Override
    public void put(Object key, Object value) {
        if (!redisHealthChecker.isRedisAvailable()) {
            log.debug("Redis недоступен, пропускаем сохранение в кэш для ключа: {}", key);
            return;
        }

        try {
            redisCache.put(key, value);
        } catch (RedisConnectionFailureException | QueryTimeoutException e) {
            handleRedisFailure(key, e);
        } catch (Exception e) {
            log.error("Неожиданная ошибка Redis при put для ключа {}: {}", key, e.getMessage());
        }
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        if (!redisHealthChecker.isRedisAvailable()) {
            log.debug("Redis недоступен, пропускаем putIfAbsent в кэш для ключа: {}", key);
            return null;
        }

        try {
            return redisCache.putIfAbsent(key, value);
        } catch (RedisConnectionFailureException | QueryTimeoutException e) {
            handleRedisFailure(key, e);
            return null;
        } catch (Exception e) {
            log.error("Неожиданная ошибка Redis при putIfAbsent для ключа {}: {}", key, e.getMessage());
            return null;
        }
    }

    @Override
    public void evict(Object key) {
        if (!redisHealthChecker.isRedisAvailable()) {
            return;
        }

        try {
            redisCache.evict(key);
        } catch (RedisConnectionFailureException | QueryTimeoutException e) {
            handleRedisFailure(key, e);
        } catch (Exception e) {
            log.error("Неожиданная ошибка Redis при evict для ключа {}: {}", key, e.getMessage());
        }
    }

    @Override
    public void clear() {
        if (!redisHealthChecker.isRedisAvailable()) {
            return;
        }

        try {
            redisCache.clear();
        } catch (RedisConnectionFailureException | QueryTimeoutException e) {
            handleRedisFailure(null, e);
        } catch (Exception e) {
            log.error("Неожиданная ошибка Redis при clear: {}", e.getMessage());
        }
    }

    private void handleRedisFailure(Object key, Exception e) {
            redisHealthChecker.setGlobalRedisStatus(false);
            log.error("Redis стал недоступен для кэша {} (ключ: {}). Ошибка: {}. Переключаемся на прямые вызовы.",
                    cacheName, key, e.getMessage());

            if (e instanceof RedisConnectionFailureException) {
                log.error("Redis connection failure. Проверьте доступность Redis сервера.");
            } else if (e instanceof QueryTimeoutException) {
                log.error("Redis timeout. Возможно, Redis перегружен или сетевые проблемы.");
            }
    }
}