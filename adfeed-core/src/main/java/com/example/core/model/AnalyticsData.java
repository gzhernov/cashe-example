package com.example.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsData {
    private Long clientId;
    private LocalDateTime timestamp;
    private Integer activeUsers;
    private Integer sessions;
    private Double bounceRate;
    private Double conversionRate;
    private BigDecimal revenue;
}