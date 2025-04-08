package com.paymentservice.controllers;

import com.paymentservice.dto.PaymentResponse;
import com.paymentservice.dto.PaymentRequest;
import com.paymentservice.services.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/check")
    public Mono<PaymentResponse> checkFunds(
        @RequestParam Integer id,
        @RequestParam Double amount) {
        return paymentService.hasSufficientFunds(id, amount)
            .map(isSufficient -> new PaymentResponse(id, isSufficient))
            .onErrorResume(e -> Mono.just(new PaymentResponse(id, false)));
    }

    @PostMapping("/pay")
    public Mono<ResponseEntity<PaymentResponse>> processPayment(@RequestBody PaymentRequest request) {
        return paymentService.processPayment(request)
            .thenReturn(ResponseEntity.ok(new PaymentResponse(request.id(),true)))
            .onErrorResume(e -> Mono.just(
                ResponseEntity.badRequest().body(new PaymentResponse(request.id(),false))
            ));
    }

}
