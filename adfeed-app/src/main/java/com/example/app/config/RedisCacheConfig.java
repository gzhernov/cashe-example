package com.example.app.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.cache.transaction.TransactionAwareCacheDecorator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@EnableScheduling
@Profile("redis")
public class RedisCacheConfig implements CacheConfigMarker {

    @Value("${spring.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.redis.port:6379}")
    private int redisPort;

    @Value("${spring.redis.password:}")
    private String redisPassword;

    @Value("${spring.redis.database:0}")
    private int redisDatabase;

    @Bean("redisObjectMapper")
    public ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // ВАЖНО: Настраиваем полиморфную типизацию для сохранения информации о классах
        BasicPolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("com.example.core.model") // Разрешаем все классы из этого пакета
                .allowIfSubType("java.util") // Разрешаем коллекции
                .allowIfSubType("java.time") // Разрешаем временные типы
                .build();

        mapper.activateDefaultTyping(
                ptv,
                ObjectMapper.DefaultTyping.NON_FINAL, // Сохраняем типы для всех нефинальных классов
                com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY // Как JSON свойство
        );

        return mapper;
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(redisHost, redisPort);
//        config.setDatabase(redisDatabase);
//        if (redisPassword != null && !redisPassword.isEmpty()) {
//            config.setPassword(redisPassword);
//        }
        return new LettuceConnectionFactory(config);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory,
                                                       @Qualifier("redisObjectMapper") ObjectMapper redisObjectMapper) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());

        GenericJackson2JsonRedisSerializer serializer =
                new GenericJackson2JsonRedisSerializer(redisObjectMapper);

        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    @Primary
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory,
                                     @Qualifier("redisObjectMapper") ObjectMapper redisObjectMapper,
                                     RedisHealthChecker redisHealthChecker) {
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5))
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new GenericJackson2JsonRedisSerializer(redisObjectMapper)));

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put("clientProfiles", defaultCacheConfig.entryTtl(Duration.ofMinutes(10)));
        cacheConfigurations.put("accountInfo", defaultCacheConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigurations.put("activeCampaigns", defaultCacheConfig.entryTtl(Duration.ofMinutes(3)));
        cacheConfigurations.put("audienceSegments", defaultCacheConfig.entryTtl(Duration.ofMinutes(15)));
        cacheConfigurations.put("performanceReports", defaultCacheConfig.entryTtl(Duration.ofMinutes(2)));
        cacheConfigurations.put("billingInfo", defaultCacheConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigurations.put("targetingSettings", defaultCacheConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("notificationSettings", defaultCacheConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("topCreatives", defaultCacheConfig.entryTtl(Duration.ofMinutes(10)));
        cacheConfigurations.put("dailyAnalytics", defaultCacheConfig.entryTtl(Duration.ofMinutes(1)));

        RedisCacheManager redisCacheManager = RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultCacheConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();

        log.info("Initializing Redis caches...");
        for (String cacheName : cacheConfigurations.keySet()) {
            // Вызов getCache принудительно создаст кэш
            redisCacheManager.getCache(cacheName);
            log.debug("Cache '{}' initialized", cacheName);
        }


        // Оборачиваем все кэши в FailoverRedisCache
        SimpleCacheManager failoverCacheManager = new SimpleCacheManager();

        failoverCacheManager.setCaches(
                redisCacheManager.getCacheNames().stream()
                        .map(cacheName -> {
                            Cache redisCache = redisCacheManager.getCache(cacheName);
                            if (redisCache instanceof RedisCache) {
                                return new FailoverRedisCache(redisHealthChecker, (RedisCache) redisCache);
                            } else if(redisCache instanceof TransactionAwareCacheDecorator tdcd && tdcd.getTargetCache() instanceof RedisCache) {
                                return new FailoverRedisCache(redisHealthChecker, (RedisCache) tdcd.getTargetCache());
                            }
                            return redisCache;
                        })
                        .collect(Collectors.toList())
        );

        failoverCacheManager.afterPropertiesSet();

        log.info("✅ Redis Cache Manager с поддержкой failover инициализирован");
        log.info("📦 Если Redis станет недоступен, приложение автоматически переключится на прямые вызовы backoffice");

        return failoverCacheManager;



    }
}