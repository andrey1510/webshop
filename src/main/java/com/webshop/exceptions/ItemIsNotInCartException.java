package com.webshop.exceptions;

public class ItemIsNotInCartException extends RuntimeException {
    public ItemIsNotInCartException(String message) {
        super(message);
    }
}
