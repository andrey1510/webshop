package com.shopservice.services;

import reactor.core.publisher.Mono;

public interface PaymentService {
    Mono<Boolean> checkFunds(Integer userId, Double amount);

    Mono<Boolean> processPayment(Integer userId, Double amount);
}
