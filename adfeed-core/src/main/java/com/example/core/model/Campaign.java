package com.example.core.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class Campaign {
    private Long id;
    private Long clientId;
    private String name;
    private String type;
    private String status;
    private BigDecimal budget;
    private BigDecimal spent;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer impressions;
    private Integer clicks;
    private Double ctr;
}