package com.paymentservice.services;

import com.paymentservice.dto.PaymentRequest;
import com.paymentservice.entities.UserBalance;
import com.paymentservice.exceptions.PaymentException;
import com.paymentservice.repositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;

    @Override
    public Mono<Double> getCurrentBalance(Integer id) {
        return paymentRepository.findById(id)
            .map(UserBalance::getBalance)
            .map(Double::valueOf)
            .defaultIfEmpty(0.0);
    }

    @Override
    public Mono<Boolean> hasSufficientFunds(Integer id, Double amount) {
        return getCurrentBalance(id)
            .map(balance -> balance.compareTo(amount) >= 0);
    }

    @Override
    public Mono<Void> processPayment(PaymentRequest request) {
        return hasSufficientFunds(request.getId(), request.getAmount())
            .flatMap(hasFunds -> {
                if (!hasFunds) {
                    return Mono.error(new PaymentException("Недостаточно средств"));
                }
                return deductFromBalance(request.getId(), request.getAmount());
            });
    }

    @Override
    public Mono<Void> deductFromBalance(Integer id, Double amount) {
        return paymentRepository.findById(id)
            .flatMap(balance -> {
                Double newBalance = balance.getBalance() - amount;
                return paymentRepository.save(
                    new UserBalance(balance.getId(), newBalance)
                );
            })
            .then();
    }

}
