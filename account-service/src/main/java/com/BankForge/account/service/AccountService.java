package com.BankForge.account.service;

import com.BankForge.account.dto.AccountResponseDto;
import com.BankForge.account.dto.CreateAccountRequestDto;
import com.BankForge.account.dto.UpdateAccountRequestDto;
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
import java.util.Set;
import java.util.UUID;

@Service
public class AccountService {
    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository){
        this.accountRepository = accountRepository;
    }

    @Transactional
    public AccountResponseDto createAccount(CreateAccountRequestDto request){
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
            int page,
            int size,
            String sortBy,
            String direction) {

        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page number cannot be negative"
            );
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and 100"
            );
        }

        Set<String> allowedSortFields = Set.of(
                "id",
                "customerId",
                "accountNumber",
                "accountType",
                "balance",
                "currency",
                "status",
                "createdAt",
                "updatedAt"
        );

        if (!allowedSortFields.contains(sortBy)) {
            throw new IllegalArgumentException(
                    "Invalid sorting field: " + sortBy
            );
        }

        Sort.Direction sortDirection;

        try {
            sortDirection = Sort.Direction.fromString(direction);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Invalid sorting direction. Use 'asc' or 'desc'"
            );
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(sortDirection, sortBy)
        );

        return accountRepository.findAll(pageable)
                .map(this::toResponse);
    }


    public AccountResponseDto updateAccount(UUID id, UpdateAccountRequestDto request){

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Account not found with ID: " + id
                ));


        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new IllegalArgumentException(
                    "Closed accounts cannot be updated"
            );
        }

        if (account.getStatus() == request.getStatus()) {
            throw new IllegalArgumentException(
                    "Account already has the requested status"
            );
        }

        if (request.getStatus() == AccountStatus.CLOSED) {
            throw new IllegalArgumentException(
                    "Use the close account operation to close an account"
            );
        }

        account.setStatus(request.getStatus());

        Account updatedAccount = accountRepository.save(account);

        return toResponse(updatedAccount);
    }

    @Transactional
    public void closeAccount(UUID id) {

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Account not found with ID: " + id
                ));

        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new IllegalArgumentException(
                    "Account is already closed"
            );
        }

        if (account.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalArgumentException(
                    "Account cannot be closed because it has a remaining balance"
            );
        }

        account.setStatus(AccountStatus.CLOSED);

        accountRepository.save(account);
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
