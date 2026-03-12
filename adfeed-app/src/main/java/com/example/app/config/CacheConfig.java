//package com.example.app.config;
//
//import org.springframework.cache.CacheManager;
//import org.springframework.cache.annotation.EnableCaching;
//import org.springframework.cache.caffeine.CaffeineCacheManager;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
//
//@Configuration
//@EnableCaching
//public class CacheConfig {
//
//    @Bean
//    @Primary
//    public CacheManager cacheManager() {
//        CaffeineCacheManager cacheManager = new CaffeineCacheManager("clientProfiles");
//        cacheManager.setAllowNullValues(false);
//        cacheManager.setCacheSpecification("maximumSize=100,expireAfterAccess=600s,recordStats");
//        return cacheManager;
//    }
//}