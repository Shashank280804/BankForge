
package com.BankForge.account.controller;

import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import com.BankForge.account.dto.AccountResponseDto;
import com.BankForge.account.dto.CreateAccountRequestDto;
import com.BankForge.account.dto.UpdateAccountRequestDto;
import com.BankForge.account.entity.AccountStatus;
import com.BankForge.account.entity.AccountType;
import com.BankForge.account.exception.AccountNotFoundException;
import com.BankForge.account.exception.GlobalExceptionHandler;
import com.BankForge.account.service.AccountService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    @Mock
    private AccountService accountService;

    private MockMvc mockMvc;

    private UUID accountId;
    private UUID customerId;

    @BeforeEach
    void setUp() {

        AccountController accountController =
                new AccountController(accountService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(accountController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        accountId = UUID.randomUUID();
        customerId = UUID.randomUUID();
    }

    private AccountResponseDto createAccountResponse() {

        AccountResponseDto response = new AccountResponseDto();

        response.setId(accountId);
        response.setCustomerId(customerId);
        response.setAccountNumber("123456789012");
        response.setAccountType(AccountType.SAVINGS);
        response.setBalance(new BigDecimal("5000.00"));
        response.setCurrency("INR");
        response.setStatus(AccountStatus.ACTIVE);
        response.setCreatedAt(LocalDateTime.now());
        response.setUpdatedAt(LocalDateTime.now());

        return response;
    }


    // 1. Create account successfully

    @Test
    void createAccount_shouldReturn201_whenRequestIsValid()
            throws Exception {

        AccountResponseDto response = createAccountResponse();

        when(accountService.createAccount(any(CreateAccountRequestDto.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "customerId": "%s",
                                    "accountType": "SAVINGS",
                                    "currency": "INR"
                                }
                                """.formatted(customerId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(accountId.toString()))
                .andExpect(jsonPath("$.customerId").value(customerId.toString()))
                .andExpect(jsonPath("$.accountNumber").value("123456789012"))
                .andExpect(jsonPath("$.accountType").value("SAVINGS"))
                .andExpect(jsonPath("$.balance").value(5000.00))
                .andExpect(jsonPath("$.currency").value("INR"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(accountService).createAccount(any(CreateAccountRequestDto.class));
    }


    // 2. Create account with missing customer ID

    @Test
    void createAccount_shouldReturn400_whenCustomerIdIsMissing()
            throws Exception {

        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "accountType": "SAVINGS",
                                    "currency": "INR"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(accountService, never())
                .createAccount(any(CreateAccountRequestDto.class));
    }


    // 3. Create account with invalid currency

    @Test
    void createAccount_shouldReturn400_whenCurrencyIsInvalid()
            throws Exception {

        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "customerId": "%s",
                                    "accountType": "SAVINGS",
                                    "currency": "india"
                                }
                                """.formatted(customerId)))
                .andExpect(status().isBadRequest());

        verify(accountService, never())
                .createAccount(any(CreateAccountRequestDto.class));
    }


    // 4. Create account with invalid account type

    @Test
    void createAccount_shouldReturn400_whenAccountTypeIsInvalid()
            throws Exception {

        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "customerId": "%s",
                                    "accountType": "PREMIUM",
                                    "currency": "INR"
                                }
                                """.formatted(customerId)))
                .andExpect(status().isBadRequest());

        verify(accountService, never())
                .createAccount(any(CreateAccountRequestDto.class));
    }


    // 5. Get account by ID successfully

    @Test
    void getAccountById_shouldReturn200_whenAccountExists()
            throws Exception {

        AccountResponseDto response = createAccountResponse();

        when(accountService.getAccountById(accountId))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/accounts/{id}", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(accountId.toString()))
                .andExpect(jsonPath("$.accountNumber").value("123456789012"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(accountService).getAccountById(accountId);
    }


    // 6. Get account by ID when account does not exist

    @Test
    void getAccountById_shouldReturn404_whenAccountDoesNotExist()
            throws Exception {

        when(accountService.getAccountById(accountId))
                .thenThrow(new AccountNotFoundException(
                        "Account not found with ID: " + accountId
                ));

        mockMvc.perform(get("/api/v1/accounts/{id}", accountId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Account not found with ID: " + accountId))
                .andExpect(jsonPath("$.path")
                        .value("/api/v1/accounts/" + accountId));

        verify(accountService).getAccountById(accountId);
    }


    // 7. Get all accounts successfully

    @Test
    void getAllAccounts_shouldReturn200_whenAccountsExist()
            throws Exception {

        AccountResponseDto response = createAccountResponse();

        Page<AccountResponseDto> accountPage = new PageImpl<>(
                List.of(response),
                PageRequest.of(
                        0,
                        10,
                        Sort.by(Sort.Direction.DESC, "createdAt")
                ),
                1
        );

        when(accountService.getAllAccounts(0, 10, "createdAt", "desc"))
                .thenReturn(accountPage);

        mockMvc.perform(get("/api/v1/accounts")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "createdAt")
                        .param("direction", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id")
                        .value(accountId.toString()))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.number").value(0));

        verify(accountService)
                .getAllAccounts(0, 10, "createdAt", "desc");
    }


    // 8. Update account successfully (controller delegates request)

    @Test
    void updateAccount_shouldReturn200_whenRequestIsValid()
            throws Exception {

        AccountResponseDto response = createAccountResponse();

        when(accountService.updateAccount(
                eq(accountId),
                any(UpdateAccountRequestDto.class)
        )).thenReturn(response);

        mockMvc.perform(put("/api/v1/accounts/{id}", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "status": "ACTIVE"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(accountId.toString()));

        verify(accountService).updateAccount(
                eq(accountId),
                any(UpdateAccountRequestDto.class)
        );
    }


    // 9. Update account with missing status

    @Test
    void updateAccount_shouldReturn400_whenStatusIsMissing()
            throws Exception {

        mockMvc.perform(put("/api/v1/accounts/{id}", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verify(accountService, never())
                .updateAccount(
                        eq(accountId),
                        any(UpdateAccountRequestDto.class)
                );
    }


    // 10. Update account when account does not exist

    @Test
    void updateAccount_shouldReturn404_whenAccountDoesNotExist()
            throws Exception {

        when(accountService.updateAccount(
                eq(accountId),
                any(UpdateAccountRequestDto.class)
        )).thenThrow(new AccountNotFoundException(
                "Account not found with ID: " + accountId
        ));

        mockMvc.perform(put("/api/v1/accounts/{id}", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "status": "ACTIVE"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path")
                        .value("/api/v1/accounts/" + accountId));
    }


    // 11. Close account successfully

    @Test
    void closeAccount_shouldReturn204_whenAccountIsClosed()
            throws Exception {

        doNothing().when(accountService).closeAccount(accountId);

        mockMvc.perform(delete("/api/v1/accounts/{id}", accountId))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(accountService).closeAccount(accountId);
    }


    // 12. Close account when account does not exist

    @Test
    void closeAccount_shouldReturn404_whenAccountDoesNotExist()
            throws Exception {

        doThrow(new AccountNotFoundException(
                "Account not found with ID: " + accountId
        )).when(accountService).closeAccount(accountId);

        mockMvc.perform(delete("/api/v1/accounts/{id}", accountId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.path")
                        .value("/api/v1/accounts/" + accountId));

        verify(accountService).closeAccount(accountId);
    }


    // 13. Close account when business rule rejects closure

    @Test
    void closeAccount_shouldReturn400_whenAccountCannotBeClosed()
            throws Exception {

        doThrow(new IllegalArgumentException(
                "Account cannot be closed because it has a remaining balance"
        )).when(accountService).closeAccount(accountId);

        mockMvc.perform(delete("/api/v1/accounts/{id}", accountId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Account cannot be closed because it has a remaining balance"));

        verify(accountService).closeAccount(accountId);
    }
}
