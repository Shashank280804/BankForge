package com.BankForge.account.dto;

import com.BankForge.account.entity.AccountStatus;
import com.BankForge.account.entity.AccountType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AccountResponseDto {
    private UUID id;
    private UUID customerId;
    private String accountNumber;
    private AccountType accountType;
    private BigDecimal balance;
    private String currency;
    private AccountStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
