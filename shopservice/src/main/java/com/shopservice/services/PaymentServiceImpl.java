package com.shopservice.services;

import com.shopservice.dto.PaymentRequest;
import com.shopservice.dto.PaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class PaymentServiceImpl implements PaymentService {

    private final WebClient webClient;

    @Override
    public Mono<Boolean> checkFunds(Integer userId, Double amount) {
        return webClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/api/payments/check")
                .queryParam("id", userId)
                .queryParam("amount", amount)
                .build())
            .retrieve()
            .bodyToMono(PaymentResponse.class)
            .map(PaymentResponse::isBalanceSufficient)
            .onErrorReturn(false);
    }

    @Override
    public Mono<Boolean> processPayment(Integer userId, Double amount) {
        return webClient.post()
            .uri("/api/payments/pay")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(new PaymentRequest(userId, amount))
            .retrieve()
            .bodyToMono(PaymentResponse.class)
            .map(PaymentResponse::isBalanceSufficient)
            .onErrorReturn(false);
    }
}
