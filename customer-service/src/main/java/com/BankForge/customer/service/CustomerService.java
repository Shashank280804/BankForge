package com.BankForge.customer.service;

import com.BankForge.customer.dto.CreateCustomerRequestDto;
import com.BankForge.customer.dto.CustomerResponseDto;
import com.BankForge.customer.entity.Customer;
import com.BankForge.customer.repository.CustomerRepository;
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
        Customer customer = new Customer();

        customer.setFullName(request.getFullName());
        customer.setEmail(request.getEmail());
        customer.setPhoneNumber(request.getPhoneNumber());

        Customer savedCustomer = customerRepository.save(customer);

        return mapToResponseDto(savedCustomer);

    }

    public CustomerResponseDto  getCustomerById(UUID id){
        Customer customer = customerRepository.findById(id).orElseThrow();
        return mapToResponseDto(customer);
    }

    public List<CustomerResponseDto> getAllCustomers(){
        return customerRepository.findAll()
                .stream()
                .map(this :: mapToResponseDto)
                .toList();
    }

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


}
