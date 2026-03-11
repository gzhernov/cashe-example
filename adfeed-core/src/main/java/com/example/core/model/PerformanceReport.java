package com.example.core.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
public class PerformanceReport {
    private Long clientId;
    private LocalDate date;
    private Integer totalImpressions;
    private Integer totalClicks;
    private Double ctr;
    private BigDecimal totalSpent;
    private BigDecimal totalBudget;
    private Integer activeCampaigns;
}