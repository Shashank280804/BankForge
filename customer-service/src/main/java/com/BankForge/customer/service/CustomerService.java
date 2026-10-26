package com.BankForge.customer.service;

import com.BankForge.customer.dto.CreateCustomerRequestDto;
import com.BankForge.customer.dto.CustomerResponseDto;
import com.BankForge.customer.dto.UpdateCustomerRequestDto;
import com.BankForge.customer.entity.Customer;
import com.BankForge.customer.exception.CustomerNotFoundException;
import com.BankForge.customer.exception.DuplicateEmailException;
import com.BankForge.customer.repository.CustomerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository){
        this.customerRepository = customerRepository;
    }

    public CustomerResponseDto createCustomer(CreateCustomerRequestDto request){
        if(customerRepository.existsByEmail(request.getEmail())){
            throw new DuplicateEmailException("Email is already registered: " + request.getEmail());
        }
        Customer customer = new Customer();

        customer.setFullName(request.getFullName());
        customer.setEmail(request.getEmail());
        customer.setPhoneNumber(request.getPhoneNumber());

        Customer savedCustomer = customerRepository.save(customer);

        return mapToResponseDto(savedCustomer);

    }

    public CustomerResponseDto  getCustomerById(UUID id){
        Customer customer = customerRepository.findById(id).orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID " + id));
        return mapToResponseDto(customer);
    }

//    public List<CustomerResponseDto> getAllCustomers(){
//        return customerRepository.findAll()
//                .stream()
//                .map(this :: mapToResponseDto)
//                .toList();
//    }

    private CustomerResponseDto mapToResponseDto(Customer customer){
        CustomerResponseDto response = new CustomerResponseDto();

        response.setId(customer.getId());
        response.setFullName(customer.getFullName());
        response.setEmail(customer.getEmail());
        response.setStatus(customer.getStatus());
        response.setPhoneNumber(customer.getPhoneNumber());
        response.setCreatedAt(customer.getCreatedAt());

        return response;
    }

    public CustomerResponseDto updateCustomer(UUID id, UpdateCustomerRequestDto request){
        Customer customer = customerRepository.findById(id).orElseThrow(()-> new CustomerNotFoundException("Customer not found with ID: " + id));

        boolean emailUsedByAnotherCustomer = customerRepository.existsByEmailAndIdNot(request.getEmail(), id);

        if(emailUsedByAnotherCustomer){
            throw new DuplicateEmailException("Email is already registered: " + request.getEmail());
        }
        customer.setFullName(request.getFullName());
        customer.setEmail(request.getEmail());
        customer.setPhoneNumber(request.getPhoneNumber());

        Customer updatedCustomer = customerRepository.save(customer);

        return mapToResponseDto(updatedCustomer);
    }

    public void deleteCustomer(UUID id){

        Customer customer = customerRepository.findById(id).orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + id));

        customerRepository.delete(customer);

    }

    public Page<CustomerResponseDto> getAllCustomers(int page, int size, String sortBy, String direction){

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        return customerRepository.findAll(pageable)
                .map(this::mapToResponseDto);

    }


}
