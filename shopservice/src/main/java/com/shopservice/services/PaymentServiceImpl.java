package com.shopservice.services;

import com.shopservice.generated.api.PaymentApi;
import com.shopservice.generated.dto.PaymentRequest;
import com.shopservice.generated.dto.PaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class PaymentServiceImpl implements PaymentService {

    private final UserService userService;

    private final PaymentApi paymentApi;

    @Override
    public Mono<Boolean> checkFunds(Double amount) {
        return userService.getCurrentUserId()
            .flatMap(userId -> {
                log.debug("Checking funds for user ID: {}", userId);
                return paymentApi.checkFunds(userId, amount)
                    .map(PaymentResponse::getIsBalanceSufficient);
            })
            .onErrorResume(e -> {
                log.error("Error checking funds", e);
                return Mono.just(false);
            });
    }

    @Override
    public Mono<Boolean> processPayment(Double amount) {
        return userService.getCurrentUserId()
            .flatMap(userId -> {
                log.debug("Processing payment for user ID: {}", userId);
                PaymentRequest paymentRequest = new PaymentRequest()
                    .id(userId)
                    .amount(amount);

                return paymentApi.processPayment(paymentRequest)
                    .map(PaymentResponse::getIsBalanceSufficient);
            })
            .onErrorResume(e -> {
                log.error("Error processing payment", e);
                return Mono.just(false);
            });
    }
}
