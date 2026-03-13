package com.example.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountInfo {
    private long clientId;
    private String accountNumber;
    private BigDecimal balance;
    private BigDecimal creditLimit;
    private String currency;
    private LocalDateTime lastTransactionDate;
    private long transactionCount;
    private String accountStatus;
}