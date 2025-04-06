package com.shopservice.exceptions;

public class CompletedCustomerOrderNotFoundException extends RuntimeException {
    public CompletedCustomerOrderNotFoundException(String message) {
        super(message);
    }
}
