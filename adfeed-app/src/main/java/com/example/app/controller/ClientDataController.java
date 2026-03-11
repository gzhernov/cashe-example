package com.example.app.controller;

import com.example.core.model.*;
import com.example.app.service.ClientDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
@Slf4j
public class ClientDataController {

    private final ClientDataService clientDataService;

    @GetMapping("/{clientId}/profile")
    public ClientProfile getClientProfile(@PathVariable Long clientId) {
        log.info("REST request for client profile: {}", clientId);
        return clientDataService.getClientProfile(clientId);
    }

    @GetMapping("/{clientId}/account")
    public AccountInfo getAccountInfo(@PathVariable Long clientId) {
        log.info("REST request for account info: {}", clientId);
        return clientDataService.getAccountInfo(clientId);
    }

    @GetMapping("/{clientId}/active-campaign")
    public Campaign getActiveCampaign(@PathVariable Long clientId) {
        log.info("REST request for active campaign: {}", clientId);
        return clientDataService.getActiveCampaign(clientId);
    }

    @GetMapping("/{clientId}/main-audience")
    public AudienceSegment getMainAudienceSegment(@PathVariable Long clientId) {
        log.info("REST request for main audience segment: {}", clientId);
        return clientDataService.getMainAudienceSegment(clientId);
    }

    @GetMapping("/{clientId}/today-performance")
    public PerformanceReport getTodayPerformanceReport(@PathVariable Long clientId) {
        log.info("REST request for today's performance report: {}", clientId);
        return clientDataService.getTodayPerformanceReport(clientId);
    }

    @GetMapping("/{clientId}/current-billing")
    public BillingInfo getCurrentBillingInfo(@PathVariable Long clientId) {
        log.info("REST request for current billing info: {}", clientId);
        return clientDataService.getCurrentBillingInfo(clientId);
    }

    @GetMapping("/{clientId}/default-targeting")
    public TargetingSettings getDefaultTargetingSettings(@PathVariable Long clientId) {
        log.info("REST request for default targeting settings: {}", clientId);
        return clientDataService.getDefaultTargetingSettings(clientId);
    }

    @GetMapping("/{clientId}/notification-settings")
    public NotificationSettings getCurrentNotificationSettings(@PathVariable Long clientId) {
        log.info("REST request for notification settings: {}", clientId);
        return clientDataService.getCurrentNotificationSettings(clientId);
    }

    @GetMapping("/{clientId}/top-creative")
    public AdCreative getTopPerformingCreative(@PathVariable Long clientId) {
        log.info("REST request for top performing creative: {}", clientId);
        return clientDataService.getTopPerformingCreative(clientId);
    }

    @GetMapping("/{clientId}/daily-analytics")
    public AnalyticsData getDailyAnalyticsSummary(@PathVariable Long clientId) {
        log.info("REST request for daily analytics summary: {}", clientId);
        return clientDataService.getDailyAnalyticsSummary(clientId);
    }
}