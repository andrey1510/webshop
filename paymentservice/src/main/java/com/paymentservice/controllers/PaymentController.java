package com.paymentservice.controllers;

import com.paymentservice.dto.PaymentResponse;
import com.paymentservice.dto.PaymentRequest;
import com.paymentservice.services.PaymentService;
import com.paymentservice.generated.api.PaymentApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PaymentController implements PaymentApi {

    private final PaymentService paymentService;

    @Override
    public Mono<ResponseEntity<PaymentResponse>> checkFunds(
        Integer id,
        Double amount,
        ServerWebExchange exchange) {
        log.info("Received checkFunds request");
        return paymentService.hasSufficientFunds(id, amount)
            .map(isSufficient -> ResponseEntity.ok(
                new PaymentResponse(id, isSufficient)))
            .onErrorResume(e -> Mono.just(
                ResponseEntity.ok(new PaymentResponse(id, false))));
    }

    @Override
    public Mono<ResponseEntity<PaymentResponse>> processPayment(
        Mono<PaymentRequest> paymentRequest,
        ServerWebExchange exchange) {
        return paymentRequest.flatMap(request ->
            paymentService.processPayment(request)
                .thenReturn(ResponseEntity.ok(
                    new PaymentResponse(request.getId(), true)))
                .onErrorResume(e -> Mono.just(
                    ResponseEntity.badRequest().body(
                        new PaymentResponse(1, false))
                )));
    }
}