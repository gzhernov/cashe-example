package com.example.app.client;

import com.example.core.model.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "backoffice-client",
        url = "${feign.client.config.backoffice-client.url}",
        configuration = com.example.app.config.FeignClientConfig.class)
public interface BackofficeClient {

    @GetMapping("/api/core/clients/{clientId}/profile")
    ClientProfile getClientProfile(@PathVariable("clientId") Long clientId);

    @GetMapping("/api/core/clients/{clientId}/account")
    AccountInfo getAccountInfo(@PathVariable("clientId") Long clientId);

    @GetMapping("/api/core/clients/{clientId}/active-campaign")
    Campaign getActiveCampaign(@PathVariable("clientId") Long clientId);

    @GetMapping("/api/core/clients/{clientId}/main-audience")
    AudienceSegment getMainAudienceSegment(@PathVariable("clientId") Long clientId);

    @GetMapping("/api/core/clients/{clientId}/today-performance")
    PerformanceReport getTodayPerformanceReport(@PathVariable("clientId") Long clientId);

    @GetMapping("/api/core/clients/{clientId}/current-billing")
    BillingInfo getCurrentBillingInfo(@PathVariable("clientId") Long clientId);

    @GetMapping("/api/core/clients/{clientId}/default-targeting")
    TargetingSettings getDefaultTargetingSettings(@PathVariable("clientId") Long clientId);

    @GetMapping("/api/core/clients/{clientId}/notification-settings")
    NotificationSettings getCurrentNotificationSettings(@PathVariable("clientId") Long clientId);

    @GetMapping("/api/core/clients/{clientId}/top-creative")
    AdCreative getTopPerformingCreative(@PathVariable("clientId") Long clientId);

    @GetMapping("/api/core/clients/{clientId}/daily-analytics")
    AnalyticsData getDailyAnalyticsSummary(@PathVariable("clientId") Long clientId);
}