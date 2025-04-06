package com.shopservice.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
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
    public ErrorResponse handleProductNotFoundException(ProductNotFoundException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    @ExceptionHandler(CompletedCustomerOrderNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ErrorResponse handleCustomerOrderNotFoundException(CompletedCustomerOrderNotFoundException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    @ExceptionHandler(ItemIsNotInCartException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ErrorResponse handleItemIsNotInCartException(ItemIsNotInCartException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    @ExceptionHandler(AlreadyInCartException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleAlreadyInCartException(AlreadyInCartException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    @ExceptionHandler(WrongQuantityException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleWrongQuantityException(WrongQuantityException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    @ExceptionHandler(CartIsEmptyException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ErrorResponse handleCartIsEmptyException(CartIsEmptyException ex) {
        return new ErrorResponse(ex.getMessage());
    }


    @AllArgsConstructor
    @Getter
    private static class ErrorResponse {
        private final String message;
    }

}
