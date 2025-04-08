package com.paymentservice.repositories;

import com.paymentservice.entities.UserBalance;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends R2dbcRepository<UserBalance, Integer> {
}
