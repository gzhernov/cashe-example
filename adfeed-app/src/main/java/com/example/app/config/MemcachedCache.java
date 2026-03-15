package com.example.app.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import net.rubyeye.xmemcached.MemcachedClient;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;

import java.util.concurrent.Callable;

@Slf4j
public class MemcachedCache implements Cache {

    private final String name;
    private final MemcachedClient memcachedClient;
    private final int expiration;
    private final ObjectMapper objectMapper;

    public MemcachedCache(String name, MemcachedClient memcachedClient, int expiration) {
        this.name = name;
        this.memcachedClient = memcachedClient;
        this.expiration = expiration;
        this.objectMapper = createTypedObjectMapper();
    }

    private ObjectMapper createTypedObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Регистрируем модуль для Java 8 времени
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // Настраиваем полиморфную типизацию для правильной десериализации
        BasicPolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("com.example.core.model")  // Разрешаем все классы из этого пакета
                .allowIfSubType("java.util")              // Разрешаем коллекции
                .allowIfSubType("java.time")              // Разрешаем временные типы
                .build();

        mapper.activateDefaultTyping(
                ptv,
                ObjectMapper.DefaultTyping.NON_FINAL,
                com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY
        );

        return mapper;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object getNativeCache() {
        return memcachedClient;
    }

    public int getExpiration() {
        return expiration;
    }

    @Override
    public ValueWrapper get(Object key) {
        String cacheKey = buildKey(key);
        try {
            String json = memcachedClient.get(cacheKey);
            if (json == null) {
                log.debug("Cache miss for key: {}", cacheKey);
                return null;
            }

            log.debug("Cache hit for key: {}, JSON: {}", cacheKey, json.substring(0, Math.min(100, json.length())));

            // ВАЖНО: Используем objectMapper с поддержкой типов
            Object value = objectMapper.readValue(json, Object.class);
            log.debug("Deserialized to class: {}", value.getClass().getName());

            return new SimpleValueWrapper(value);
        } catch (Exception e) {
            log.error("Failed to get from Memcached cache for key: {}", cacheKey, e);
            return null;
        }
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        String cacheKey = buildKey(key);
        try {
            String json = memcachedClient.get(cacheKey);
            if (json == null) {
                return null;
            }

            // Используем указанный тип для десериализации
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            log.error("Failed to get typed value from Memcached cache for key: {}", cacheKey, e);
            return null;
        }
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        try {
            // Сначала пробуем получить из кэша
            ValueWrapper valueWrapper = get(key);
            if (valueWrapper != null) {
                return (T) valueWrapper.get();
            }

            // Если нет в кэше, загружаем через valueLoader
            log.debug("Loading value for key: {} using valueLoader", key);
            T value = valueLoader.call();
            put(key, value);
            return value;
        } catch (Exception e) {
            throw new ValueRetrievalException(key, valueLoader, e);
        }
    }

    @Override
    public void put(Object key, Object value) {
        if (value == null) {
            evict(key);
            return;
        }

        String cacheKey = buildKey(key);
        try {
            // Сериализуем с сохранением информации о типе
            String json = objectMapper.writeValueAsString(value);
            log.debug("Putting value to cache: {}, class: {}", cacheKey, value.getClass().getName());
            memcachedClient.set(cacheKey, expiration, json);
        } catch (Exception e) {
            log.error("Failed to put in Memcached cache for key: {}", cacheKey, e);
        }
    }

    @Override
    public void evict(Object key) {
        String cacheKey = buildKey(key);
        try {
            memcachedClient.delete(cacheKey);
            log.debug("Evicted cache key: {}", cacheKey);
        } catch (Exception e) {
            log.error("Failed to evict from Memcached cache for key: {}", cacheKey, e);
        }
    }

    @Override
    public void clear() {
        try {
            memcachedClient.flushAll();
            log.info("Cleared all Memcached caches");
        } catch (Exception e) {
            log.error("Failed to clear Memcached cache", e);
        }
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        String cacheKey = buildKey(key);
        try {
            // Пытаемся добавить, только если ключ не существует
            String json = objectMapper.writeValueAsString(value);
            boolean added = memcachedClient.add(cacheKey, expiration, json);

            if (added) {
                log.debug("Added new cache entry for key: {}", cacheKey);
                return null;
            } else {
                // Если ключ уже существует, возвращаем существующее значение
                Object existingValue = get(key).get();
                return new SimpleValueWrapper(existingValue);
            }
        } catch (Exception e) {
            log.error("Failed to putIfAbsent in Memcached cache for key: {}", cacheKey, e);
            return null;
        }
    }

    private String buildKey(Object key) {
        return name + ":" + key.toString();
    }
}