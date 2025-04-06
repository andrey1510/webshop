package com.shopservice.exceptions;

public class MaxImageSizeExceededException extends RuntimeException {
    public MaxImageSizeExceededException (String message) {
        super(message);
    }
}
