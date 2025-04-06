package com.shopservice.exceptions;

public class WrongImageTypeException extends RuntimeException {
    public WrongImageTypeException (String message) {
        super(message);
    }
}
