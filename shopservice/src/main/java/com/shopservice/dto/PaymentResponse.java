package com.shopservice.dto;

public record PaymentResponse(
    Integer id,
    Boolean isBalanceSufficient
) {}
