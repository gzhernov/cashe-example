package com.example.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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