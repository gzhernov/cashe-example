package com.example.core.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdCreative {
    private Long id;
    private Long clientId;
    private String name;
    private String type;
    private String headline;
    private String description;
    private String imageUrl;
    private String destinationUrl;
    private String callToAction;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime lastModified;
}