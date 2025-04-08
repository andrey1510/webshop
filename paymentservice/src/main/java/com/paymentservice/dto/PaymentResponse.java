package com.paymentservice.dto;

public record PaymentResponse(
    Integer id,
    Boolean isBalanceSufficient
) {}
