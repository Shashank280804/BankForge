package com.BankForge.account.exception;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApiErrorResponse {

    private LocalDateTime timeStamp;
    private int status;
    private String error;
    private String message;
    private String path;
}
