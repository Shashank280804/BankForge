package com.BankForge.account.service;

import com.BankForge.account.dto.AccountResponseDto;
import com.BankForge.account.dto.CreateAccountRequestDto;
import com.BankForge.account.dto.UpdateAccountRequestDto;
import com.BankForge.account.entity.Account;
import com.BankForge.account.entity.AccountStatus;
import com.BankForge.account.entity.AccountType;
import com.BankForge.account.exception.AccountNotFoundException;
import com.BankForge.account.repository.AccountRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;


    // 1. Successfully close an account

    @Test
    void closeAccount_shouldCloseAccount_whenBalanceIsZero() {

        UUID id = UUID.randomUUID();

        Account account = new Account();
        account.setId(id);
        account.setBalance(BigDecimal.ZERO);
        account.setStatus(AccountStatus.ACTIVE);

        when(accountRepository.findById(id))
                .thenReturn(Optional.of(account));

        accountService.closeAccount(id);

        assertEquals(AccountStatus.CLOSED, account.getStatus());

        verify(accountRepository).save(account);
    }


    // 2. Account not found

    @Test
    void closeAccount_shouldThrowException_whenAccountDoesNotExist() {

        UUID id = UUID.randomUUID();

        when(accountRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(
                AccountNotFoundException.class,
                () -> accountService.closeAccount(id)
        );

        verify(accountRepository, never()).save(any(Account.class));
    }


    // 3. Cannot close an account with positive balance

    @Test
    void closeAccount_shouldThrowException_whenBalanceIsPositive() {

        UUID id = UUID.randomUUID();

        Account account = new Account();
        account.setId(id);
        account.setBalance(new BigDecimal("5000.00"));
        account.setStatus(AccountStatus.ACTIVE);

        when(accountRepository.findById(id))
                .thenReturn(Optional.of(account));

        assertThrows(
                IllegalArgumentException.class,
                () -> accountService.closeAccount(id)
        );

        assertEquals(AccountStatus.ACTIVE, account.getStatus());

        verify(accountRepository, never()).save(any(Account.class));
    }


    // 4. Cannot close an already closed account

    @Test
    void closeAccount_shouldThrowException_whenAccountIsAlreadyClosed() {

        UUID id = UUID.randomUUID();

        Account account = new Account();
        account.setId(id);
        account.setBalance(BigDecimal.ZERO);
        account.setStatus(AccountStatus.CLOSED);

        when(accountRepository.findById(id))
                .thenReturn(Optional.of(account));

        assertThrows(
                IllegalArgumentException.class,
                () -> accountService.closeAccount(id)
        );

        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void createAccount_shouldCreateAccountSuccessfully() {

        UUID customerId = UUID.randomUUID();

        CreateAccountRequestDto request = new CreateAccountRequestDto();
        request.setCustomerId(customerId);
        request.setAccountType(AccountType.SAVINGS);
        request.setCurrency("INR");

        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AccountResponseDto response = accountService.createAccount(request);

        assertNotNull(response);
        assertEquals(customerId, response.getCustomerId());
        assertEquals(AccountType.SAVINGS, response.getAccountType());
        assertEquals("INR", response.getCurrency());
        assertEquals(BigDecimal.ZERO, response.getBalance());
        assertEquals(AccountStatus.ACTIVE, response.getStatus());
        assertNotNull(response.getAccountNumber());

        verify(accountRepository).save(any(Account.class));
    }
    @Test
    void getAccountById_shouldReturnAccount_whenAccountExists() {

        UUID id = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        Account account = new Account();
        account.setId(id);
        account.setCustomerId(customerId);
        account.setAccountNumber("123456789012");
        account.setAccountType(AccountType.SAVINGS);
        account.setBalance(new BigDecimal("5000.00"));
        account.setCurrency("INR");
        account.setStatus(AccountStatus.ACTIVE);

        when(accountRepository.findById(id))
                .thenReturn(Optional.of(account));

        AccountResponseDto response = accountService.getAccountById(id);

        assertNotNull(response);
        assertEquals(id, response.getId());
        assertEquals(customerId, response.getCustomerId());
        assertEquals("123456789012", response.getAccountNumber());
        assertEquals(new BigDecimal("5000.00"), response.getBalance());

        verify(accountRepository).findById(id);
    }

    @Test
    void getAccountById_shouldThrowException_whenAccountDoesNotExist() {

        UUID id = UUID.randomUUID();

        when(accountRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(
                AccountNotFoundException.class,
                () -> accountService.getAccountById(id)
        );

        verify(accountRepository).findById(id);
    }

    @Test
    void getAllAccounts_shouldReturnPaginatedAccounts() {

        Account account = new Account();
        account.setId(UUID.randomUUID());
        account.setCustomerId(UUID.randomUUID());
        account.setAccountNumber("123456789012");
        account.setAccountType(AccountType.SAVINGS);
        account.setBalance(BigDecimal.ZERO);
        account.setCurrency("INR");
        account.setStatus(AccountStatus.ACTIVE);

        List<Account> accounts = List.of(account);

        Page<Account> accountPage = new PageImpl<>(
                accounts,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")),
                1
        );

        when(accountRepository.findAll(any(Pageable.class)))
                .thenReturn(accountPage);

        Page<AccountResponseDto> response =
                accountService.getAllAccounts(0, 10, "createdAt", "desc");

        assertEquals(1, response.getTotalElements());
        assertEquals(0, response.getNumber());
        assertEquals(10, response.getSize());
        assertEquals("123456789012",
                response.getContent().get(0).getAccountNumber());

        verify(accountRepository).findAll(any(Pageable.class));
    }

    @Test
    void getAllAccounts_shouldThrowException_whenPageIsNegative() {

        assertThrows(
                IllegalArgumentException.class,
                () -> accountService.getAllAccounts(
                        -1, 10, "createdAt", "desc"
                )
        );

        verify(accountRepository, never()).findAll(any(Pageable.class));
    }


// 10. Invalid page size: zero

    @Test
    void getAllAccounts_shouldThrowException_whenPageSizeIsZero() {

        assertThrows(
                IllegalArgumentException.class,
                () -> accountService.getAllAccounts(
                        0, 0, "createdAt", "desc"
                )
        );

        verify(accountRepository, never()).findAll(any(Pageable.class));
    }


// 11. Invalid page size: greater than 100

    @Test
    void getAllAccounts_shouldThrowException_whenPageSizeExceedsLimit() {

        assertThrows(
                IllegalArgumentException.class,
                () -> accountService.getAllAccounts(
                        0, 101, "createdAt", "desc"
                )
        );

        verify(accountRepository, never()).findAll(any(Pageable.class));
    }


// 12. Invalid sorting field

    @Test
    void getAllAccounts_shouldThrowException_whenSortFieldIsInvalid() {

        assertThrows(
                IllegalArgumentException.class,
                () -> accountService.getAllAccounts(
                        0, 10, "invalidField", "desc"
                )
        );

        verify(accountRepository, never()).findAll(any(Pageable.class));
    }


// 13. Invalid sorting direction

    @Test
    void getAllAccounts_shouldThrowException_whenSortDirectionIsInvalid() {

        assertThrows(
                IllegalArgumentException.class,
                () -> accountService.getAllAccounts(
                        0, 10, "createdAt", "wrong"
                )
        );

        verify(accountRepository, never()).findAll(any(Pageable.class));
    }


// 14. Update should throw exception when account does not exist

    @Test
    void updateAccount_shouldThrowException_whenAccountDoesNotExist() {

        UUID id = UUID.randomUUID();

        UpdateAccountRequestDto request = new UpdateAccountRequestDto();
        request.setStatus(AccountStatus.CLOSED);

        when(accountRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(
                AccountNotFoundException.class,
                () -> accountService.updateAccount(id, request)
        );

        verify(accountRepository, never()).save(any(Account.class));
    }


// 15. Update should reject an already closed account

    @Test
    void updateAccount_shouldThrowException_whenAccountIsAlreadyClosed() {

        UUID id = UUID.randomUUID();

        Account account = new Account();
        account.setId(id);
        account.setBalance(BigDecimal.ZERO);
        account.setStatus(AccountStatus.CLOSED);

        UpdateAccountRequestDto request = new UpdateAccountRequestDto();
        request.setStatus(AccountStatus.ACTIVE);

        when(accountRepository.findById(id))
                .thenReturn(Optional.of(account));

        assertThrows(
                IllegalArgumentException.class,
                () -> accountService.updateAccount(id, request)
        );

        verify(accountRepository, never()).save(any(Account.class));
    }


// 16. Update should reject the same status

    @Test
    void updateAccount_shouldThrowException_whenRequestedStatusIsSame() {

        UUID id = UUID.randomUUID();

        Account account = new Account();
        account.setId(id);
        account.setBalance(BigDecimal.ZERO);
        account.setStatus(AccountStatus.ACTIVE);

        UpdateAccountRequestDto request = new UpdateAccountRequestDto();
        request.setStatus(AccountStatus.ACTIVE);

        when(accountRepository.findById(id))
                .thenReturn(Optional.of(account));

        assertThrows(
                IllegalArgumentException.class,
                () -> accountService.updateAccount(id, request)
        );

        verify(accountRepository, never()).save(any(Account.class));
    }


// 17. Update should reject closure through PUT endpoint

    @Test
    void updateAccount_shouldThrowException_whenRequestAttemptsToCloseAccount() {

        UUID id = UUID.randomUUID();

        Account account = new Account();
        account.setId(id);
        account.setBalance(BigDecimal.ZERO);
        account.setStatus(AccountStatus.ACTIVE);

        UpdateAccountRequestDto request = new UpdateAccountRequestDto();
        request.setStatus(AccountStatus.CLOSED);

        when(accountRepository.findById(id))
                .thenReturn(Optional.of(account));

        assertThrows(
                IllegalArgumentException.class,
                () -> accountService.updateAccount(id, request)
        );

        verify(accountRepository, never()).save(any(Account.class));
    }



}