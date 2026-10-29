package com.BankForge.account.service;

import com.BankForge.account.dto.AccountResponseDto;
import com.BankForge.account.dto.CreateAccountRequest;
import com.BankForge.account.entity.Account;
import com.BankForge.account.entity.AccountStatus;
import com.BankForge.account.exception.AccountNotFoundException;
import com.BankForge.account.repository.AccountRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class AccountService {
    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository){
        this.accountRepository = accountRepository;
    }

    @Transactional
    public AccountResponseDto createAccount(CreateAccountRequest request){
        Account account = new Account();

        account.setCustomerId(request.getCustomerId());
        account.setAccountType(request.getAccountType());
        account.setCurrency(request.getCurrency());
        account.setAccountNumber(generateAccountNumber());
        account.setStatus(AccountStatus.ACTIVE);
        account.setBalance(BigDecimal.ZERO);

        Account savedAccount = accountRepository.save(account);

        return toResponse(savedAccount);


    }

    public AccountResponseDto getAccountById(UUID id){
        Account account = accountRepository.findById(id).orElseThrow(()-> new AccountNotFoundException(
                "Account not found with ID: " + id
        ));

        return toResponse(account);

    }

    public Page<AccountResponseDto> getAllAccounts(
            int page, int size, String sortBy, String direction) {

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(sortDirection, sortBy)
        );

        return accountRepository.findAll(pageable)
                .map(this::toResponse);
    }

    private String generateAccountNumber(){
        return UUID.randomUUID()
                .toString()
                .replace("_","")
                .substring(0,12)
                .toUpperCase();
    }

    private AccountResponseDto toResponse(Account account){
        AccountResponseDto response = new AccountResponseDto();

        response.setId(account.getId());
        response.setCustomerId(account.getCustomerId());
        response.setAccountNumber(account.getAccountNumber());
        response.setAccountType(account.getAccountType());
        response.setBalance(account.getBalance());
        response.setCurrency(account.getCurrency());
        response.setStatus(account.getStatus());
        response.setCreatedAt(account.getCreatedAt());
        response.setUpdatedAt(account.getUpdatedAt());

        return response;
    }
}
