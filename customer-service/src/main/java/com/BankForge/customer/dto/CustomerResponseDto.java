package com.BankForge.customer.dto;

import com.BankForge.customer.entity.CustomerStatus;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class CustomerResponseDto {
    private UUID id;

    private String fullName;

    private String email;

    private CustomerStatus status;

    private String phoneNumber;

    private Instant createdAt;
}
