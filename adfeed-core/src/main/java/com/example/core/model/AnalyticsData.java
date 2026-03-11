package com.example.core.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AnalyticsData {
    private Long clientId;
    private LocalDateTime timestamp;
    private Integer activeUsers;
    private Integer sessions;
    private Double bounceRate;
    private Double conversionRate;
    private BigDecimal revenue;
}