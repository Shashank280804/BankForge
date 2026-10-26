package com.BankForge.customer.controller;

import com.BankForge.customer.dto.CreateCustomerRequestDto;
import com.BankForge.customer.dto.CustomerResponseDto;
import com.BankForge.customer.dto.UpdateCustomerRequestDto;
import com.BankForge.customer.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {
    private final CustomerService customerService;

    public CustomerController(CustomerService customerService){
        this.customerService = customerService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponseDto createCustomer(@Valid @RequestBody CreateCustomerRequestDto request){

        return customerService.createCustomer(request);

    }

    @GetMapping("/{id}")
    public CustomerResponseDto getCustomerById(@PathVariable UUID id){
        return customerService.getCustomerById(id);

    }

//    @GetMapping
//    public List<CustomerResponseDto> getAllCustomers(){
//        return customerService.getAllCustomers();
//    }

    @PutMapping("/{id}")
    public CustomerResponseDto updateCustomer(@PathVariable UUID id, @Valid @RequestBody UpdateCustomerRequestDto request){

        return customerService.updateCustomer(id, request);

    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCustomer(@PathVariable UUID id){
        customerService.deleteCustomer(id);
    }

    @GetMapping
    public Page<CustomerResponseDto> getAllCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction){
        return customerService.getAllCustomers(page, size, sortBy, direction);

    }



}
