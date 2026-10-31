package com.BankForge.account.dto;

import com.BankForge.account.entity.AccountStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateAccountRequestDto {

    @NotNull(message = "Account status is required")
    private AccountStatus status;
}
