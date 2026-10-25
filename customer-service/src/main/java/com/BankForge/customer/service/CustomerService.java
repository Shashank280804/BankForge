package com.BankForge.customer.service;

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

    public Customer createCustomer(Customer customer){
        return customerRepository.save(customer);
    }

    public Customer getCustomerById(UUID id){
        return customerRepository.findById(id).orElseThrow();
    }

    public List<Customer> getAllCustomers(){
        return customerRepository.findAll();
    }


}
