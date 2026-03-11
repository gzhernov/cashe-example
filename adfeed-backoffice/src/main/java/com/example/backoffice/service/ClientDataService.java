package com.example.backoffice.service;

import com.example.core.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class ClientDataService {

    // Имитация задержки для всех методов
    private void simulateDelay() {
        try {
            TimeUnit.MILLISECONDS.sleep(5000); // 500ms задержка для демонстрации
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public ClientProfile getClientProfile(Long clientId) {
        simulateDelay();
        log.info("Fetching client profile for clientId: {}", clientId);

        return ClientProfile.builder()
                .id(clientId)
                .name("Клиент " + clientId)
                .email("client" + clientId + "@example.com")
                .phone("+7-999-123-45-6" + clientId)
                .company("Компания " + clientId)
                .position("Менеджер")
                .registrationDate(LocalDateTime.now().minusMonths(clientId % 12))
                .status(clientId % 3 == 0 ? "ACTIVE" : "PENDING")
                .build();
    }

    public AccountInfo getAccountInfo(Long clientId) {
        simulateDelay();
        log.info("Fetching account info for clientId: {}", clientId);

        return AccountInfo.builder()
                .clientId(clientId)
                .accountNumber("ACC" + String.format("%08d", clientId))
                .balance(BigDecimal.valueOf(10000 + clientId * 1000))
                .creditLimit(BigDecimal.valueOf(50000))
                .currency("RUB")
                .lastTransactionDate(LocalDateTime.now().minusDays(clientId % 30))
                .transactionCount(50 + clientId)
                .accountStatus(clientId % 2 == 0 ? "ACTIVE" : "FROZEN")
                .build();
    }

    public Campaign getActiveCampaign(Long clientId) {
        simulateDelay();
        log.info("Fetching active campaign for clientId: {}", clientId);

        return Campaign.builder()
                .id((long) (1 + clientId * 100))
                .clientId(clientId)
                .name("Основная кампания для клиента " + clientId)
                .type(clientId % 2 == 0 ? "DISPLAY" : "VIDEO")
                .status("ACTIVE")
                .budget(BigDecimal.valueOf(50000 + clientId * 10000))
                .spent(BigDecimal.valueOf(15000 + clientId * 5000))
                .startDate(LocalDateTime.now().minusDays(30))
                .endDate(LocalDateTime.now().plusDays(30))
                .impressions(10000)
                .clicks(500)
                .ctr(5.0)
                .build();
    }

    public AudienceSegment getMainAudienceSegment(Long clientId) {
        simulateDelay();
        log.info("Fetching main audience segment for clientId: {}", clientId);

        return AudienceSegment.builder()
                .id(1L)
                .name("Основной сегмент аудитории")
                .description("Целевая аудитория по умолчанию")
                .size(15000)
                .estimatedReach(12000.0)
                .status("ACTIVE")
                .build();
    }

    public PerformanceReport getTodayPerformanceReport(Long clientId) {
        simulateDelay();
        log.info("Fetching today's performance report for clientId: {}", clientId);

        return PerformanceReport.builder()
                .clientId(clientId)
                .date(LocalDate.now())
                .totalImpressions(50000 + clientId.intValue() * 1000)
                .totalClicks(2500 + clientId.intValue() * 50)
                .ctr(5.0)
                .totalSpent(BigDecimal.valueOf(25000 + clientId * 500))
                .totalBudget(BigDecimal.valueOf(100000))
                .activeCampaigns(3)
                .build();
    }

    public BillingInfo getCurrentBillingInfo(Long clientId) {
        simulateDelay();
        log.info("Fetching current billing info for clientId: {}", clientId);

        return BillingInfo.builder()
                .clientId(clientId)
                .paymentMethod(clientId % 2 == 0 ? "CREDIT_CARD" : "BANK_TRANSFER")
                .cardLastFour("****" + String.format("%04d", 1000 + clientId))
                .nextBillingDate(LocalDateTime.now().plusDays(15))
                .outstandingBalance(BigDecimal.valueOf(1500 + clientId * 100))
                .lastPayment(BigDecimal.valueOf(3000))
                .lastPaymentDate(LocalDateTime.now().minusDays(clientId % 20))
                .billingStatus(clientId % 5 == 0 ? "OVERDUE" : "CURRENT")
                .build();
    }

    public TargetingSettings getDefaultTargetingSettings(Long clientId) {
        simulateDelay();
        log.info("Fetching default targeting settings for clientId: {}", clientId);

        return TargetingSettings.builder()
                .clientId(clientId)
                .ageRange("25-45")
                .gender("ALL")
                .language("RU")
                .timezone("Europe/Moscow")
                .build();
    }

    public NotificationSettings getCurrentNotificationSettings(Long clientId) {
        simulateDelay();
        log.info("Fetching current notification settings for clientId: {}", clientId);

        return NotificationSettings.builder()
                .clientId(clientId)
                .emailNotifications(true)
                .smsNotifications(clientId % 2 == 0)
                .pushNotifications(true)
                .notificationFrequency("DAILY")
                .dailyDigest(true)
                .weeklyReport(true)
                .build();
    }

    public AdCreative getTopPerformingCreative(Long clientId) {
        simulateDelay();
        log.info("Fetching top performing creative for clientId: {}", clientId);

        return AdCreative.builder()
                .id((long) (1 + clientId * 100))
                .clientId(clientId)
                .name("Лучший креатив для клиента " + clientId)
                .type("VIDEO")
                .headline("Уникальное предложение для вас")
                .description("Описание лучшего креатива")
                .imageUrl("/images/best_creative.jpg")
                .destinationUrl("https://example.com/landing/" + clientId)
                .callToAction("Купить сейчас")
                .status("ACTIVE")
                .createdAt(LocalDateTime.now().minusDays(30))
                .lastModified(LocalDateTime.now().minusDays(1))
                .build();
    }

    public AnalyticsData getDailyAnalyticsSummary(Long clientId) {
        simulateDelay();
        log.info("Fetching daily analytics summary for clientId: {}", clientId);

        return AnalyticsData.builder()
                .clientId(clientId)
                .timestamp(LocalDateTime.now())
                .activeUsers(1000 + clientId.intValue() * 50)
                .sessions(2500 + clientId.intValue() * 100)
                .bounceRate(35.0 + (clientId % 20))
                .conversionRate(2.5 + (clientId % 3))
                .revenue(BigDecimal.valueOf(50000 + clientId * 1000))
                .build();
    }
}