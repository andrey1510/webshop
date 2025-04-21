package com.shopservice.services;

import com.shopservice.generated.api.PaymentApi;
import com.shopservice.generated.dto.PaymentRequest;
import com.shopservice.generated.dto.PaymentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentApi paymentApi;

    @Mock
    private UserService userService;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private final Integer TEST_USER_ID = 1;
    private final Double TEST_AMOUNT = 100.0;
    private final Double TEST_LARGE_AMOUNT = 1500.0;

    @BeforeEach
    void setUp() {
        when(userService.getCurrentUserId()).thenReturn(Mono.just(TEST_USER_ID));
    }

    @Test
    void checkFunds() {
        when(paymentApi.checkFunds(TEST_USER_ID, TEST_AMOUNT))
            .thenReturn(Mono.just(new PaymentResponse()
                .id(TEST_USER_ID)
                .isBalanceSufficient(true)));

        StepVerifier.create(paymentService.checkFunds(TEST_AMOUNT))
            .expectNext(true)
            .verifyComplete();
    }

    @Test
    void checkFunds_BalanceInsufficient() {
        when(paymentApi.checkFunds(TEST_USER_ID, TEST_LARGE_AMOUNT))
            .thenReturn(Mono.just(new PaymentResponse()
                .id(TEST_USER_ID)
                .isBalanceSufficient(false)));

        StepVerifier.create(paymentService.checkFunds(TEST_LARGE_AMOUNT))
            .expectNext(false)
            .verifyComplete();
    }

    @Test
    void checkFunds_PaymentServiceFails() {
        when(paymentApi.checkFunds(TEST_USER_ID, TEST_AMOUNT))
            .thenReturn(Mono.error(new RuntimeException("Service unavailable")));

        StepVerifier.create(paymentService.checkFunds(TEST_AMOUNT))
            .expectNext(false)
            .verifyComplete();
    }

    @Test
    void processPayment() {
        PaymentRequest expectedRequest = new PaymentRequest()
            .id(TEST_USER_ID)
            .amount(TEST_AMOUNT);

        when(paymentApi.processPayment(expectedRequest))
            .thenReturn(Mono.just(new PaymentResponse()
                .id(TEST_USER_ID)
                .isBalanceSufficient(true)));

        StepVerifier.create(paymentService.processPayment(TEST_AMOUNT))
            .expectNext(true)
            .verifyComplete();
    }

    @Test
    void processPayment_ExceedBalance() {
        PaymentRequest expectedRequest = new PaymentRequest()
            .id(TEST_USER_ID)
            .amount(TEST_LARGE_AMOUNT);

        when(paymentApi.processPayment(expectedRequest))
            .thenReturn(Mono.just(new PaymentResponse()
                .id(TEST_USER_ID)
                .isBalanceSufficient(false)));

        StepVerifier.create(paymentService.processPayment(TEST_LARGE_AMOUNT))
            .expectNext(false)
            .verifyComplete();
    }

    @Test
    void processPayment_PaymentServiceFails() {
        PaymentRequest expectedRequest = new PaymentRequest()
            .id(TEST_USER_ID)
            .amount(TEST_AMOUNT);

        when(paymentApi.processPayment(expectedRequest))
            .thenReturn(Mono.error(new WebClientResponseException(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "Service Unavailable",
                null, null, null)));

        StepVerifier.create(paymentService.processPayment(TEST_AMOUNT))
            .expectNext(false)
            .verifyComplete();
    }
}