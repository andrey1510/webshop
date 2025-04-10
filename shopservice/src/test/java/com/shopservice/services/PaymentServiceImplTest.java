package com.shopservice.services;

import com.shopservice.dto.PaymentRequest;
import com.shopservice.dto.PaymentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class PaymentServiceImplTest {

    @Autowired
    private WebTestClient webTestClient;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec<?> requestHeadersUriSpec;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Autowired
    private PaymentServiceImpl paymentService;

    private final Integer TEST_USER_ID = 1;
    private final Double TEST_AMOUNT = 100.0;
    private final Double TEST_LARGE_AMOUNT = 1500.0;

    @BeforeEach
    void setUp() {
        // Настройка моков WebClient
        //when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.contentType(any())).thenReturn(requestBodyUriSpec);
        //when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    void checkFunds_shouldReturnTrue_whenPaymentServiceReturnsSuccess() {
        // Arrange
        when(responseSpec.bodyToMono(PaymentResponse.class))
            .thenReturn(Mono.just(new PaymentResponse(TEST_USER_ID, true)));

        // Act & Assert
        StepVerifier.create(paymentService.checkFunds(TEST_USER_ID, TEST_AMOUNT))
            .expectNext(true)
            .verifyComplete();
    }

    @Test
    void checkFunds_shouldReturnFalse_whenPaymentServiceReturnsFail() {
        // Arrange
        when(responseSpec.bodyToMono(PaymentResponse.class))
            .thenReturn(Mono.just(new PaymentResponse(TEST_USER_ID, false)));

        // Act & Assert
        StepVerifier.create(paymentService.checkFunds(TEST_USER_ID, TEST_LARGE_AMOUNT))
            .expectNext(false)
            .verifyComplete();
    }

    @Test
    void checkFunds_shouldReturnFalse_whenPaymentServiceFails() {
        // Arrange
        when(responseSpec.bodyToMono(PaymentResponse.class))
            .thenReturn(Mono.error(new RuntimeException("Service unavailable")));

        // Act & Assert
        StepVerifier.create(paymentService.checkFunds(TEST_USER_ID, TEST_AMOUNT))
            .expectNext(false)
            .verifyComplete();
    }

    @Test
    void processPayment_shouldReturnTrue_whenPaymentSuccessful() {
        // Arrange
        when(responseSpec.bodyToMono(PaymentResponse.class))
            .thenReturn(Mono.just(new PaymentResponse(TEST_USER_ID, true)));

        // Act & Assert
        StepVerifier.create(paymentService.processPayment(TEST_USER_ID, TEST_AMOUNT))
            .expectNext(true)
            .verifyComplete();
    }

    @Test
    void processPayment_shouldReturnFalse_whenPaymentFailed() {
        // Arrange
        when(responseSpec.bodyToMono(PaymentResponse.class))
            .thenReturn(Mono.just(new PaymentResponse(TEST_USER_ID, false)));

        // Act & Assert
        StepVerifier.create(paymentService.processPayment(TEST_USER_ID, TEST_LARGE_AMOUNT))
            .expectNext(false)
            .verifyComplete();
    }

    @Test
    void processPayment_shouldReturnFalse_whenServiceUnavailable() {
        // Arrange
        when(responseSpec.bodyToMono(PaymentResponse.class))
            .thenReturn(Mono.error(new WebClientResponseException(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "Service Unavailable",
                null, null, null)));

        // Act & Assert
        StepVerifier.create(paymentService.processPayment(TEST_USER_ID, TEST_AMOUNT))
            .expectNext(false)
            .verifyComplete();
    }

    @Test
    void paymentEndpoint_shouldReturnSuccess_whenFundsAvailable() {
        // Arrange
        when(responseSpec.bodyToMono(PaymentResponse.class))
            .thenReturn(Mono.just(new PaymentResponse(TEST_USER_ID, true)));

        // Act & Assert
        webTestClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/api/payments/check")
                .queryParam("id", TEST_USER_ID)
                .queryParam("amount", TEST_AMOUNT)
                .build())
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(TEST_USER_ID)
            .jsonPath("$.isBalanceSufficient").isEqualTo(true);
    }

    @Test
    void paymentEndpoint_shouldReturnBadRequest_whenInsufficientFunds() {
        // Arrange
        when(responseSpec.bodyToMono(PaymentResponse.class))
            .thenReturn(Mono.just(new PaymentResponse(TEST_USER_ID, false)));

        // Act & Assert
        webTestClient.post()
            .uri("/api/payments/pay")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(new PaymentRequest(TEST_USER_ID, TEST_LARGE_AMOUNT))
            .exchange()
            .expectStatus().isBadRequest()
            .expectBody()
            .jsonPath("$.id").isEqualTo(TEST_USER_ID)
            .jsonPath("$.isBalanceSufficient").isEqualTo(false);
    }
}