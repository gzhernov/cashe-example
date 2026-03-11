package com.example.core.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TargetingSettings {
    private Long clientId;
    private List<String> geoLocations;
    private List<String> devices;
    private List<String> platforms;
    private List<String> interests;
    private String ageRange;
    private String gender;
    private String language;
    private String timezone;
}