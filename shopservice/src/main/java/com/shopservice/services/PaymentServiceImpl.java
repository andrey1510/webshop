package com.shopservice.services;

import com.shopservice.generated.api.PaymentApi;
import com.shopservice.generated.dto.PaymentRequest;
import com.shopservice.generated.dto.PaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentApi paymentApi;

    @Override
    public Mono<Boolean> checkFunds(Integer userId, Double amount) {
        return paymentApi.checkFunds(userId, amount)
            .map(PaymentResponse::getIsBalanceSufficient)
            .onErrorReturn(false);
    }

    @Override
    public Mono<Boolean> processPayment(Integer userId, Double amount) {
        PaymentRequest paymentRequest = new PaymentRequest()
            .id(userId)
            .amount(amount);

        return paymentApi.processPayment(paymentRequest)
            .map(PaymentResponse::getIsBalanceSufficient)
            .onErrorReturn(false);
    }
}
