package com.shopservice.exceptions;

public class AlreadyInCartException extends RuntimeException {
    public AlreadyInCartException(String message) {
        super(message);
    }
}
