package com.webshop.controllers;

import com.webshop.exceptions.MaxImageSizeExceededException;
import com.webshop.exceptions.ProductNotFoundException;
import com.webshop.exceptions.WrongImageTypeException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
@RequiredArgsConstructor
public class ExceptionsHandler {

    @ExceptionHandler(WrongImageTypeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleWrongImageTypeException(WrongImageTypeException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    @ExceptionHandler(MaxImageSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleMaxImageSizeExceededException(MaxImageSizeExceededException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    @ExceptionHandler(ProductNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ErrorResponse handlePostNotFoundException(ProductNotFoundException ex) {
        return new ErrorResponse(ex.getMessage());
    }


    @AllArgsConstructor
    @Getter
    private static class ErrorResponse {
        private final String message;
    }

}
