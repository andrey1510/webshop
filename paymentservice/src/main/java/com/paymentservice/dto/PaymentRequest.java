package com.paymentservice.dto;

public record PaymentRequest(
    Integer id,
    Double amount
) {}
