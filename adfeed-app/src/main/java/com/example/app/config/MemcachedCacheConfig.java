package com.example.app.config;

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.command.BinaryCommandFactory;
import net.rubyeye.xmemcached.impl.KetamaMemcachedSessionLocator;
import net.rubyeye.xmemcached.utils.AddrUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.io.IOException;
import java.util.List;

@Configuration
@Profile("memcached")
@EnableCaching
public class MemcachedCacheConfig implements CacheConfigMarker {

    @Value("${memcached.servers:localhost:11211}")
    private String memcachedServers;

    @Value("${memcached.connection-pool-size:5}")
    private int connectionPoolSize;

    @Value("${memcached.timeout:3000}")
    private int operationTimeout;

    @Value("${memcached.connect-timeout:3000}")
    private int connectTimeout;

    @Value("${memcached.heal-session-interval:5000}")
    private int healSessionInterval;

    @Value("${memcached.failure-mode:false}")
    private boolean failureMode;

    // TTL для разных кэшей с дефолтными значениями
    @Value("${memcached.ttl.clientProfiles:600}")
    private int clientProfilesTtl;

    @Value("${memcached.ttl.accountInfo:300}")
    private int accountInfoTtl;

    @Value("${memcached.ttl.activeCampaigns:180}")
    private int activeCampaignsTtl;

    @Value("${memcached.ttl.audienceSegments:900}")
    private int audienceSegmentsTtl;

    @Value("${memcached.ttl.performanceReports:120}")
    private int performanceReportsTtl;

    @Value("${memcached.ttl.billingInfo:300}")
    private int billingInfoTtl;

    @Value("${memcached.ttl.targetingSettings:1800}")
    private int targetingSettingsTtl;

    @Value("${memcached.ttl.notificationSettings:1800}")
    private int notificationSettingsTtl;

    @Value("${memcached.ttl.topCreatives:600}")
    private int topCreativesTtl;

    @Value("${memcached.ttl.dailyAnalytics:60}")
    private int dailyAnalyticsTtl;

    @Bean
    public MemcachedClient memcachedClient() throws IOException {
        XMemcachedClientBuilder builder = new XMemcachedClientBuilder(
                AddrUtil.getAddresses(memcachedServers));

        // Настройки из конфига
        builder.setConnectionPoolSize(connectionPoolSize);
        builder.setCommandFactory(new BinaryCommandFactory());
        builder.setSessionLocator(new KetamaMemcachedSessionLocator());
        builder.setConnectTimeout(connectTimeout);
        builder.setOpTimeout(operationTimeout);
        builder.setHealSessionInterval(healSessionInterval);
        builder.setFailureMode(failureMode);

        // Дополнительные настройки для надежности
        builder.setEnableHealSession(true);

        MemcachedClient client = builder.build();
        client.setEnableHealSession(true);
        client.setSanitizeKeys(false);

        return client;
    }

    @Bean
    @Primary
    public CacheManager cacheManager(MemcachedClient memcachedClient) {
        SimpleCacheManager cacheManager = new SimpleCacheManager();

        // Создаем список кэшей с TTL из конфига
        List<MemcachedCache> caches = List.of(
                new MemcachedCache("clientProfiles", memcachedClient, clientProfilesTtl),
                new MemcachedCache("accountInfo", memcachedClient, accountInfoTtl),
                new MemcachedCache("activeCampaigns", memcachedClient, activeCampaignsTtl),
                new MemcachedCache("audienceSegments", memcachedClient, audienceSegmentsTtl),
                new MemcachedCache("performanceReports", memcachedClient, performanceReportsTtl),
                new MemcachedCache("billingInfo", memcachedClient, billingInfoTtl),
                new MemcachedCache("targetingSettings", memcachedClient, targetingSettingsTtl),
                new MemcachedCache("notificationSettings", memcachedClient, notificationSettingsTtl),
                new MemcachedCache("topCreatives", memcachedClient, topCreativesTtl),
                new MemcachedCache("dailyAnalytics", memcachedClient, dailyAnalyticsTtl)
        );

        cacheManager.setCaches(caches);
        cacheManager.afterPropertiesSet(); // Важно для инициализации

        return cacheManager;
    }
}