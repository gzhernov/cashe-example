package com.example.core.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class BillingInfo {
    private Long clientId;
    private String paymentMethod;
    private String cardLastFour;
    private LocalDateTime nextBillingDate;
    private BigDecimal outstandingBalance;
    private BigDecimal lastPayment;
    private LocalDateTime lastPaymentDate;
    private String billingStatus;
}