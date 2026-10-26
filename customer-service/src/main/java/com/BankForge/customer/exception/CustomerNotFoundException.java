package com.BankForge.customer.exception;

import org.springframework.web.bind.annotation.RestController;

public class CustomerNotFoundException extends RuntimeException{

    public CustomerNotFoundException(String message){
        super(message);
    }


}
