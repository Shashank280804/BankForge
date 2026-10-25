package com.BankForge.customer.repository;

import com.BankForge.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer,UUID> {

}
