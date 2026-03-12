package com.example.app.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.spring.cache.CacheConfig;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisCacheConfig {

    @Value("${spring.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.redis.port:6379}")
    private int redisPort;

    @Value("${spring.redis.password:}")
    private String redisPassword;

    @Value("${spring.redis.database:0}")
    private int redisDatabase;

    @Bean
    public ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        return mapper;
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(redisHost, redisPort);
        config.setDatabase(redisDatabase);
        if (redisPassword != null && !redisPassword.isEmpty()) {
            config.setPassword(redisPassword);
        }
        return new LettuceConnectionFactory(config);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory,
                                                       ObjectMapper redisObjectMapper) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer(redisObjectMapper));
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer(redisObjectMapper));
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    @Primary
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory,
                                     ObjectMapper redisObjectMapper) {
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5)) // TTL по умолчанию
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new GenericJackson2JsonRedisSerializer(redisObjectMapper)));

        // Индивидуальные настройки для разных кэшей
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

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultCacheConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }

    // Альтернативный вариант с Redisson (более продвинутый)
    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://" + redisHost + ":" + redisPort)
                .setDatabase(redisDatabase)
                .setConnectionPoolSize(10)
                .setConnectionMinimumIdleSize(5);

        if (redisPassword != null && !redisPassword.isEmpty()) {
            config.useSingleServer().setPassword(redisPassword);
        }

        return Redisson.create(config);
    }

    @Bean
    public CacheManager redissonCacheManager(RedissonClient redissonClient) {
        Map<String, CacheConfig> config = new HashMap<>();

        // Настройки TTL и max idle для каждого кэша
        config.put("clientProfiles", new CacheConfig(10 * 60 * 1000, 5 * 60 * 1000)); // 10 min ttl, 5 min max idle
        config.put("accountInfo", new CacheConfig(5 * 60 * 1000, 3 * 60 * 1000));
        config.put("activeCampaigns", new CacheConfig(3 * 60 * 1000, 2 * 60 * 1000));
        config.put("audienceSegments", new CacheConfig(15 * 60 * 1000, 10 * 60 * 1000));
        config.put("performanceReports", new CacheConfig(2 * 60 * 1000, 1 * 60 * 1000));
        config.put("billingInfo", new CacheConfig(5 * 60 * 1000, 3 * 60 * 1000));
        config.put("targetingSettings", new CacheConfig(30 * 60 * 1000, 15 * 60 * 1000));
        config.put("notificationSettings", new CacheConfig(30 * 60 * 1000, 15 * 60 * 1000));
        config.put("topCreatives", new CacheConfig(10 * 60 * 1000, 5 * 60 * 1000));
        config.put("dailyAnalytics", new CacheConfig(1 * 60 * 1000, 30 * 1000));

        return new RedissonSpringCacheManager(redissonClient, config);
    }
}