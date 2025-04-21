package com.shopservice.services;

import reactor.core.publisher.Mono;

public interface PaymentService {
    Mono<Boolean> checkFunds(Double amount);

    Mono<Boolean> processPayment(Double amount);
}
