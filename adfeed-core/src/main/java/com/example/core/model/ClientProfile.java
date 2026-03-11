package com.example.core.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ClientProfile {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String company;
    private String position;
    private LocalDateTime registrationDate;
    private String status;
}