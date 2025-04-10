package com.paymentservice.services;

import com.paymentservice.dto.PaymentRequest;
import com.paymentservice.repositories.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import com.paymentservice.entities.UserBalance;
import com.paymentservice.exceptions.PaymentException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private UserBalance userWithBalance;
    private UserBalance userWithLowBalance;
    private PaymentRequest sufficientPaymentRequest;
    private PaymentRequest insufficientPaymentRequest;
    private final Integer TEST_USER_ID = 1;
    private final Double TEST_BALANCE = 100.0;
    private final Double TEST_LOW_BALANCE = 30.0;
    private final Double TEST_SMALL_AMOUNT = 50.0;
    private final Double TEST_LARGE_AMOUNT = 150.0;

    @BeforeEach
    void setUp() {

        userWithBalance = new UserBalance(TEST_USER_ID, TEST_BALANCE);
        userWithLowBalance = new UserBalance(TEST_USER_ID, TEST_LOW_BALANCE);
        sufficientPaymentRequest = new PaymentRequest(TEST_USER_ID, TEST_SMALL_AMOUNT);
        insufficientPaymentRequest = new PaymentRequest(TEST_USER_ID, TEST_LARGE_AMOUNT);
    }

    @Test
    void testGetCurrentBalance() {
        when(paymentRepository.findById(TEST_USER_ID))
            .thenReturn(Mono.just(userWithBalance));

        StepVerifier.create(paymentService.getCurrentBalance(TEST_USER_ID))
            .expectNext(TEST_BALANCE)
            .verifyComplete();
    }

    @Test
    void hasSufficientFunds() {
        when(paymentRepository.findById(TEST_USER_ID))
            .thenReturn(Mono.just(userWithBalance));

        StepVerifier.create(paymentService.hasSufficientFunds(TEST_USER_ID, TEST_SMALL_AMOUNT))
            .expectNext(true)
            .verifyComplete();
    }

    @Test
    void testHasSufficientFunds_BalanceInsufficient() {
        when(paymentRepository.findById(TEST_USER_ID))
            .thenReturn(Mono.just(userWithLowBalance));

        StepVerifier.create(paymentService.hasSufficientFunds(TEST_USER_ID, TEST_SMALL_AMOUNT))
            .expectNext(false)
            .verifyComplete();
    }

    @Test
    void testProcessPayment() {
        when(paymentRepository.findById(TEST_USER_ID))
            .thenReturn(Mono.just(userWithBalance));
        when(paymentRepository.save(any(UserBalance.class)))
            .thenReturn(Mono.just(new UserBalance(TEST_USER_ID, TEST_BALANCE - TEST_SMALL_AMOUNT)));

        StepVerifier.create(paymentService.processPayment(sufficientPaymentRequest))
            .verifyComplete();
    }

    @Test
    void testProcessPayment_InsufficientFunds() {
        when(paymentRepository.findById(TEST_USER_ID))
            .thenReturn(Mono.just(userWithLowBalance));

        StepVerifier.create(paymentService.processPayment(insufficientPaymentRequest))
            .expectErrorMatches(ex ->
                ex instanceof PaymentException &&
                    ex.getMessage().equals("Недостаточно средств"))
            .verify();
    }

    @Test
    void testDeductFromBalance() {
        UserBalance updatedBalance = new UserBalance(TEST_USER_ID, TEST_BALANCE - TEST_SMALL_AMOUNT);

        when(paymentRepository.findById(TEST_USER_ID))
            .thenReturn(Mono.just(userWithBalance));
        when(paymentRepository.save(updatedBalance))
            .thenReturn(Mono.just(updatedBalance));

        StepVerifier.create(paymentService.deductFromBalance(TEST_USER_ID, TEST_SMALL_AMOUNT))
            .verifyComplete();
    }

}
