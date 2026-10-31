package com.BankForge.account.controller;

import com.BankForge.account.dto.AccountResponseDto;
import com.BankForge.account.dto.CreateAccountRequestDto;
import com.BankForge.account.dto.UpdateAccountRequestDto;
import com.BankForge.account.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/v1/accounts")
@Tag(
        name = "Accounts",
        description = "APIs for creating, retrieving, updating, and closing bank accounts"
)
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService){
        this.accountService = accountService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create an account",
            description = "Creates a new bank account using the provided customer and account details."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Account created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Customer not found"),
            @ApiResponse(responseCode = "409", description = "Account number conflict")
    })
    public AccountResponseDto createAccount(@Valid @RequestBody CreateAccountRequestDto request){
        return  accountService.createAccount(request);

    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get account by ID",
            description = "Retrieves an account using its unique account ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public AccountResponseDto getAccountById(@PathVariable UUID id){
        return accountService.getAccountById(id);
    }


    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get all accounts",
            description = "Retrieves accounts using pagination and sorting."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Accounts retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination or sorting parameters")
    })
    public Page<AccountResponseDto> getAllAccounts(
            @Parameter(description = "Page number, starting from 0")
            @Min(value = 0, message = "Page number cannot be negative")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Number of records per page (1-100)")
            @Min(value = 1, message = "Page size must be at least 1")
            @Max(value = 100, message = "Page size cannot exceed 100")
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ){
       return accountService.getAllAccounts(page, size, sortBy, direction);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Update an account",
            description = "Updates account information using the provided account details."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or account state"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public AccountResponseDto updateAccount(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAccountRequestDto request) {

        return accountService.updateAccount(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Close an account",
            description = "Closes an account without physically deleting its database record."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Account closed successfully"),
            @ApiResponse(responseCode = "400", description = "Account cannot be closed"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public void closeAccount(@PathVariable UUID id) {
        accountService.closeAccount(id);
    }


}
