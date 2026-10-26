
package com.BankForge.customer.service;

import com.BankForge.customer.dto.CreateCustomerRequestDto;
import com.BankForge.customer.dto.CustomerResponseDto;
import com.BankForge.customer.dto.UpdateCustomerRequestDto;
import com.BankForge.customer.entity.Customer;
import com.BankForge.customer.entity.CustomerStatus;
import com.BankForge.customer.exception.CustomerNotFoundException;
import com.BankForge.customer.exception.DuplicateEmailException;
import com.BankForge.customer.repository.CustomerRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    // Test 1: Successfully create a customer
    @Test
    void createCustomer_shouldReturnCustomer_whenRequestIsValid() {
        CreateCustomerRequestDto request = createRequest(
                "Rahul Sharma", "rahul@example.com", "9876543210");

        Customer savedCustomer = createCustomer(
                "Rahul Sharma", "rahul@example.com", "9876543210");

        when(customerRepository.existsByEmail("rahul@example.com"))
                .thenReturn(false);
        when(customerRepository.save(any(Customer.class)))
                .thenReturn(savedCustomer);

        CustomerResponseDto response = customerService.createCustomer(request);

        assertNotNull(response);
        assertEquals("Rahul Sharma", response.getFullName());
        assertEquals("rahul@example.com", response.getEmail());

        verify(customerRepository).existsByEmail("rahul@example.com");
        verify(customerRepository).save(any(Customer.class));
    }

    // Test 2: Reject a duplicate email during creation
    @Test
    void createCustomer_shouldThrowException_whenEmailAlreadyExists() {
        CreateCustomerRequestDto request = createRequest(
                "Rahul Sharma", "rahul@example.com", "9876543210");

        when(customerRepository.existsByEmail("rahul@example.com"))
                .thenReturn(true);

        assertThrows(
                DuplicateEmailException.class,
                () -> customerService.createCustomer(request)
        );

        verify(customerRepository).existsByEmail("rahul@example.com");
        verify(customerRepository, never()).save(any(Customer.class));
    }

    // Test 3: Successfully retrieve a customer by ID
    @Test
    void getCustomerById_shouldReturnCustomer_whenCustomerExists() {
        UUID id = UUID.randomUUID();
        Customer customer = createCustomer(
                "Rahul Sharma", "rahul@example.com", "9876543210");
        customer.setId(id);

        when(customerRepository.findById(id))
                .thenReturn(Optional.of(customer));

        CustomerResponseDto response = customerService.getCustomerById(id);

        assertNotNull(response);
        assertEquals(id, response.getId());
        assertEquals("Rahul Sharma", response.getFullName());
        assertEquals("rahul@example.com", response.getEmail());

        verify(customerRepository).findById(id);
    }

    // Test 4: Throw an exception when the customer does not exist
    @Test
    void getCustomerById_shouldThrowException_whenCustomerDoesNotExist() {
        UUID id = UUID.randomUUID();

        when(customerRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(
                CustomerNotFoundException.class,
                () -> customerService.getCustomerById(id)
        );

        verify(customerRepository).findById(id);
    }

    // Test 5: Successfully update a customer
    @Test
    void updateCustomer_shouldReturnUpdatedCustomer_whenRequestIsValid() {
        UUID id = UUID.randomUUID();

        Customer existingCustomer = createCustomer(
                "Rahul Sharma", "rahul@example.com", "9876543210");
        existingCustomer.setId(id);

        UpdateCustomerRequestDto request = new UpdateCustomerRequestDto();
        request.setFullName("Rahul Verma");
        request.setEmail("rahul.verma@example.com");
        request.setPhoneNumber("9123456780");

        when(customerRepository.findById(id))
                .thenReturn(Optional.of(existingCustomer));
        when(customerRepository.existsByEmailAndIdNot(
                "rahul.verma@example.com", id))
                .thenReturn(false);
        when(customerRepository.save(any(Customer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CustomerResponseDto response = customerService.updateCustomer(id, request);

        assertNotNull(response);
        assertEquals(id, response.getId());
        assertEquals("Rahul Verma", response.getFullName());
        assertEquals("rahul.verma@example.com", response.getEmail());
        assertEquals("9123456780", response.getPhoneNumber());

        verify(customerRepository).findById(id);
        verify(customerRepository).existsByEmailAndIdNot(
                "rahul.verma@example.com", id);
        verify(customerRepository).save(existingCustomer);
    }

    // Test 6: Reject an email already used by another customer
    @Test
    void updateCustomer_shouldThrowException_whenEmailBelongsToAnotherCustomer() {
        UUID id = UUID.randomUUID();

        Customer existingCustomer = createCustomer(
                "Rahul Sharma", "rahul@example.com", "9876543210");
        existingCustomer.setId(id);

        UpdateCustomerRequestDto request = new UpdateCustomerRequestDto();
        request.setFullName("Rahul Verma");
        request.setEmail("existing@example.com");
        request.setPhoneNumber("9123456780");

        when(customerRepository.findById(id))
                .thenReturn(Optional.of(existingCustomer));
        when(customerRepository.existsByEmailAndIdNot(
                "existing@example.com", id))
                .thenReturn(true);

        assertThrows(
                DuplicateEmailException.class,
                () -> customerService.updateCustomer(id, request)
        );

        verify(customerRepository, never()).save(any(Customer.class));
    }

    // Test 7: Successfully delete an existing customer
    @Test
    void deleteCustomer_shouldDeleteCustomer_whenCustomerExists() {
        UUID id = UUID.randomUUID();

        Customer customer = createCustomer(
                "Rahul Sharma", "rahul@example.com", "9876543210");
        customer.setId(id);

        when(customerRepository.findById(id))
                .thenReturn(Optional.of(customer));

        customerService.deleteCustomer(id);

        verify(customerRepository).findById(id);
        verify(customerRepository).delete(customer);
    }

    // Test 8: Throw an exception when deleting a nonexistent customer
    @Test
    void deleteCustomer_shouldThrowException_whenCustomerDoesNotExist() {
        UUID id = UUID.randomUUID();

        when(customerRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(
                CustomerNotFoundException.class,
                () -> customerService.deleteCustomer(id)
        );

        verify(customerRepository, never()).delete(any(Customer.class));
    }

    // Helper method: create a request DTO
    private CreateCustomerRequestDto createRequest(
            String fullName, String email, String phoneNumber) {

        CreateCustomerRequestDto request = new CreateCustomerRequestDto();
        request.setFullName(fullName);
        request.setEmail(email);
        request.setPhoneNumber(phoneNumber);
        return request;
    }

    // Helper method: create a Customer entity
    private Customer createCustomer(
            String fullName, String email, String phoneNumber) {

        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        customer.setFullName(fullName);
        customer.setEmail(email);
        customer.setPhoneNumber(phoneNumber);
        customer.setStatus(CustomerStatus.ACTIVE);
        return customer;
    }
}
