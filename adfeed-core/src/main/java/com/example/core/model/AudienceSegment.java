package com.example.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AudienceSegment {
    private Long id;
    private String name;
    private String description;
    private Integer size;
    private List<String> targetingCriteria;
    private Double estimatedReach;
    private String status;
}