
package com.BankForge.customer.controller;

import com.BankForge.customer.dto.CreateCustomerRequestDto;
import com.BankForge.customer.dto.CustomerResponseDto;
import com.BankForge.customer.entity.CustomerStatus;
import com.BankForge.customer.exception.CustomerNotFoundException;
import com.BankForge.customer.service.CustomerService;

import tools.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CustomerService customerService;

    @Test
    void createCustomer_shouldReturn201_whenRequestIsValid() throws Exception {
        UUID id = UUID.randomUUID();

        CreateCustomerRequestDto request = new CreateCustomerRequestDto();
        request.setFullName("Rahul Sharma");
        request.setEmail("rahul@example.com");
        request.setPhoneNumber("9876543210");

        CustomerResponseDto response = new CustomerResponseDto();
        response.setId(id);
        response.setFullName("Rahul Sharma");
        response.setEmail("rahul@example.com");
        response.setStatus(CustomerStatus.ACTIVE);
        response.setPhoneNumber("9876543210");

        when(customerService.createCustomer(any(CreateCustomerRequestDto.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.fullName").value("Rahul Sharma"))
                .andExpect(jsonPath("$.email").value("rahul@example.com"));

        verify(customerService).createCustomer(any(CreateCustomerRequestDto.class));
    }

    @Test
    void createCustomer_shouldReturn400_whenEmailIsInvalid() throws Exception {
        String invalidRequest = """
                {
                    "fullName": "Rahul Sharma",
                    "email": "not-an-email",
                    "phoneNumber": "9876543210"
                }
                """;

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(customerService);
    }


    @Test
    void getCustomerById_shouldReturn200_whenCustomerExists() throws Exception {
        UUID id = UUID.randomUUID();

        CustomerResponseDto response = new CustomerResponseDto();
        response.setId(id);
        response.setFullName("Rahul Sharma");
        response.setEmail("rahul@example.com");
        response.setStatus(CustomerStatus.ACTIVE);
        response.setPhoneNumber("9876543210");

        when(customerService.getCustomerById(id)).thenReturn(response);

        mockMvc.perform(get("/api/v1/customers/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.fullName").value("Rahul Sharma"))
                .andExpect(jsonPath("$.email").value("rahul@example.com"));
    }

    @Test
    void deleteCustomer_shouldReturn204_whenCustomerExists() throws Exception {
        UUID id = UUID.randomUUID();

        doNothing().when(customerService).deleteCustomer(id);

        mockMvc.perform(delete("/api/v1/customers/{id}", id))
                .andExpect(status().isNoContent());

        verify(customerService).deleteCustomer(id);
    }


    @Test
    void getCustomerById_shouldReturn404_whenCustomerDoesNotExist()
            throws Exception {

        UUID id = UUID.randomUUID();

        when(customerService.getCustomerById(id))
                .thenThrow(new CustomerNotFoundException(
                        "Customer not found with ID: " + id));

        mockMvc.perform(get("/api/v1/customers/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value(
                        "Customer not found with ID: " + id))
                .andExpect(jsonPath("$.path").value(
                        "/api/v1/customers/" + id));
    }
}
