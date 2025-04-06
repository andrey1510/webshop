package com.shopservice.exceptions;

public class WrongQuantityException extends RuntimeException {
    public WrongQuantityException(String message) {
        super(message);
    }
}

