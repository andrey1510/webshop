package com.shopservice.repositories;

import com.shopservice.configs.TestDatabaseConfig;
import com.shopservice.configs.TestSecurityConfig;
import com.shopservice.entities.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataR2dbcTest
@ActiveProfiles("test")
@Import({TestDatabaseConfig.class, TestSecurityConfig.class})
@SpringJUnitConfig
class CustomerOrderRepositoryTest {

    @Autowired
    private CustomerOrderRepository customerOrderRepository;

    @Test
    void findByStatus() {
        StepVerifier.create(customerOrderRepository.findByStatus(OrderStatus.CART))
            .expectNextMatches(order -> {
                assertEquals(OrderStatus.CART, order.getStatus());
                assertEquals(6, order.getId());
                return true;
            })
            .verifyComplete();
    }

    @Test
    void findByIdAndStatus() {
        StepVerifier.create(customerOrderRepository.findByIdAndStatus(7, OrderStatus.COMPLETED))
            .expectNextMatches(order -> {
                assertEquals(OrderStatus.COMPLETED, order.getStatus());
                assertEquals(1500.0, order.getCompletedOrderPrice());
                return true;
            })
            .verifyComplete();
    }

    @Test
    void findByIdAndStatus_WrongStatus() {
        StepVerifier.create(customerOrderRepository.findByIdAndStatus(6, OrderStatus.COMPLETED))
            .verifyComplete();
    }

    @Test
    void findAllByStatus() {
        StepVerifier.create(customerOrderRepository.findAllByStatus(OrderStatus.COMPLETED).collectList())
            .expectNextMatches(orders -> {
                if (orders.size() != 2) return false;
                return orders.stream()
                    .allMatch(order -> order.getStatus() == OrderStatus.COMPLETED);
            })
            .verifyComplete();
    }

    @Test
    void findByIdAndStatus_WrongId() {
        StepVerifier.create(customerOrderRepository.findByIdAndStatus(999, OrderStatus.COMPLETED))
            .verifyComplete();
    }
}

