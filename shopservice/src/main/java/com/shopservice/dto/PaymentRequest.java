package com.shopservice.dto;

public record PaymentRequest(
    Integer id,
    Double amount
) {}