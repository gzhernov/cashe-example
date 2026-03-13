package com.example.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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