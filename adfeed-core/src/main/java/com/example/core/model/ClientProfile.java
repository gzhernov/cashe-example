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