package com.webshop.exceptions;

public class CompletedCustomerOrderNotFoundException extends RuntimeException {
    public CompletedCustomerOrderNotFoundException(String message) {
        super(message);
    }
}
