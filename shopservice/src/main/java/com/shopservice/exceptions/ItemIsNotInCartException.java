package com.shopservice.exceptions;

public class ItemIsNotInCartException extends RuntimeException {
    public ItemIsNotInCartException(String message) {
        super(message);
    }
}
