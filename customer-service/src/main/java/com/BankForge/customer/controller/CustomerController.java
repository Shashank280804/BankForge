package com.BankForge.customer.controller;

import com.BankForge.customer.dto.CreateCustomerRequestDto;
import com.BankForge.customer.dto.CustomerResponseDto;
import com.BankForge.customer.dto.UpdateCustomerRequestDto;
import com.BankForge.customer.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@Tag(
        name = "Customers",
        description = "APIs for creating, retrieving, updating, and deleting customers"
)
@RequestMapping("/api/v1/customers")
public class CustomerController {
    private final CustomerService customerService;

    public CustomerController(CustomerService customerService){
        this.customerService = customerService;
    }

    @Operation(
            summary = "Create a customer",
            description = "Registers a new customer using the provided customer details."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Customer created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "409", description = "Email is already registered")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponseDto createCustomer(@Valid @RequestBody CreateCustomerRequestDto request){

        return customerService.createCustomer(request);

    }

    @Operation(
            summary = "Get customer by ID",
            description = "Retrieves a customer's details using their UUID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Customer retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @GetMapping("/{id}")
    public CustomerResponseDto getCustomerById(@PathVariable UUID id){
        return customerService.getCustomerById(id);

    }

//    @GetMapping
//    public List<CustomerResponseDto> getAllCustomers(){
//        return customerService.getAllCustomers();
//    }

    @Operation(
            summary = "Update a customer",
            description = "Updates an existing customer's name, email, and phone number."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Customer updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Customer not found"),
            @ApiResponse(responseCode = "409", description = "Email is already registered")
    })
    @PutMapping("/{id}")
    public CustomerResponseDto updateCustomer(@PathVariable UUID id, @Valid @RequestBody UpdateCustomerRequestDto request){

        return customerService.updateCustomer(id, request);

    }

    @Operation(
            summary = "Delete a customer",
            description = "Permanently deletes a customer by UUID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Customer deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCustomer(@PathVariable UUID id){
        customerService.deleteCustomer(id);
    }

    @Operation(
            summary = "Get all customers",
            description = "Returns a paginated list of customers with configurable sorting."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Customers retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination or sorting parameters")
    })
    @GetMapping
    public Page<CustomerResponseDto> getAllCustomers(

            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Page must be zero or greater")
            int page,
            @Min(value = 1, message = "Page size must be at least 1")
            @Max(value = 100, message = "Page size cannot exceed 100")
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction){
        return customerService.getAllCustomers(page, size, sortBy, direction);

    }



}
