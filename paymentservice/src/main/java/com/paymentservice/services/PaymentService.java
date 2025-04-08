package com.paymentservice.services;

import com.paymentservice.dto.PaymentRequest;
import reactor.core.publisher.Mono;


public interface PaymentService {
    Mono<Double> getCurrentBalance(Integer userId);

    Mono<Boolean> hasSufficientFunds(Integer id, Double amount);

    Mono<Void> processPayment(PaymentRequest request);

    Mono<Void> deductFromBalance(Integer id, Double amount);

}
