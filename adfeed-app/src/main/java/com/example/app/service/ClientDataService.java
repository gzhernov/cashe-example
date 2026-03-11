package com.example.app.service;

import com.example.app.client.BackofficeClient;
import com.example.core.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientDataService {

    private final BackofficeClient backofficeClient;

    @Cacheable(value = "clientProfiles", key = "#clientId", unless = "#result == null")
    public ClientProfile getClientProfile(Long clientId) {
        log.info("Fetching client profile from backoffice for clientId: {}", clientId);
        return backofficeClient.getClientProfile(clientId);
    }

    @Cacheable(value = "accountInfo", key = "#clientId", unless = "#result == null")
    public AccountInfo getAccountInfo(Long clientId) {
        log.info("Fetching account info from backoffice for clientId: {}", clientId);
        return backofficeClient.getAccountInfo(clientId);
    }

    @Cacheable(value = "activeCampaigns", key = "#clientId", unless = "#result == null")
    public Campaign getActiveCampaign(Long clientId) {
        log.info("Fetching active campaign from backoffice for clientId: {}", clientId);
        return backofficeClient.getActiveCampaign(clientId);
    }

    @Cacheable(value = "audienceSegments", key = "#clientId", unless = "#result == null")
    public AudienceSegment getMainAudienceSegment(Long clientId) {
        log.info("Fetching main audience segment from backoffice for clientId: {}", clientId);
        return backofficeClient.getMainAudienceSegment(clientId);
    }

    @Cacheable(value = "performanceReports", key = "#clientId", unless = "#result == null")
    public PerformanceReport getTodayPerformanceReport(Long clientId) {
        log.info("Fetching performance report from backoffice for clientId: {}", clientId);
        return backofficeClient.getTodayPerformanceReport(clientId);
    }

    @Cacheable(value = "billingInfo", key = "#clientId", unless = "#result == null")
    public BillingInfo getCurrentBillingInfo(Long clientId) {
        log.info("Fetching billing info from backoffice for clientId: {}", clientId);
        return backofficeClient.getCurrentBillingInfo(clientId);
    }

    @Cacheable(value = "targetingSettings", key = "#clientId", unless = "#result == null")
    public TargetingSettings getDefaultTargetingSettings(Long clientId) {


        ClientProfile.builder()
                .id(clientId)
                .name("Клиент " + clientId)
                .email("client" + clientId + "@example.com")
                .phone("+7-999-123-45-6" + clientId)
                .company("Компания " + clientId)
                .position("Менеджер")
                .registrationDate(LocalDateTime.now().minusMonths(clientId % 12))
                .status(clientId % 3 == 0 ? "ACTIVE" : "PENDING")
                .build();



        log.info("Fetching targeting settings from backoffice for clientId: {}", clientId);
        return backofficeClient.getDefaultTargetingSettings(clientId);
    }

    @Cacheable(value = "notificationSettings", key = "#clientId", unless = "#result == null")
    public NotificationSettings getCurrentNotificationSettings(Long clientId) {
        log.info("Fetching notification settings from backoffice for clientId: {}", clientId);
        return backofficeClient.getCurrentNotificationSettings(clientId);
    }

    @Cacheable(value = "topCreatives", key = "#clientId", unless = "#result == null")
    public AdCreative getTopPerformingCreative(Long clientId) {
        log.info("Fetching top creative from backoffice for clientId: {}", clientId);
        return backofficeClient.getTopPerformingCreative(clientId);
    }

    @Cacheable(value = "dailyAnalytics", key = "#clientId", unless = "#result == null")
    public AnalyticsData getDailyAnalyticsSummary(Long clientId) {
        log.info("Fetching daily analytics from backoffice for clientId: {}", clientId);
        return backofficeClient.getDailyAnalyticsSummary(clientId);
    }
}