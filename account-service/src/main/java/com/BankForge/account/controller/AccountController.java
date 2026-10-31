package com.BankForge.account.controller;

import com.BankForge.account.dto.AccountResponseDto;
import com.BankForge.account.dto.CreateAccountRequestDto;
import com.BankForge.account.dto.UpdateAccountRequestDto;
import com.BankForge.account.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService){
        this.accountService = accountService;
    }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponseDto createAccount(@Valid @RequestBody CreateAccountRequestDto request){
        return  accountService.createAccount(request);

    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public AccountResponseDto getAccountById(@PathVariable UUID id){
        return accountService.getAccountById(id);
    }


    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Page<AccountResponseDto> getAllAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ){
       return accountService.getAllAccounts(page, size, sortBy, direction);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public AccountResponseDto updateAccount(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAccountRequestDto request) {

        return accountService.updateAccount(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void closeAccount(@PathVariable UUID id) {
        accountService.closeAccount(id);
    }


}
