package com.example.core.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AudienceSegment {
    private Long id;
    private String name;
    private String description;
    private Integer size;
    private List<String> targetingCriteria;
    private Double estimatedReach;
    private String status;
}