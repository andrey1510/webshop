package com.webshop.exceptions;

public class MaxImageSizeExceededException extends RuntimeException {
    public MaxImageSizeExceededException (String message) {
        super(message);
    }
}
