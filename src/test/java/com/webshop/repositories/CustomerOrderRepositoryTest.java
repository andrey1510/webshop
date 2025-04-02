package com.webshop.repositories;

import com.webshop.entities.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataR2dbcTest
@ActiveProfiles("test")
@SpringJUnitConfig
class CustomerOrderRepositoryTest {

    @Autowired
    private CustomerOrderRepository customerOrderRepository;

    @Test
    void testFindByStatus() {
        StepVerifier.create(customerOrderRepository.findByStatus(OrderStatus.CART))
            .expectNextMatches(order -> {
                assertEquals(OrderStatus.CART, order.getStatus());
                assertEquals(6, order.getId());
                return true;
            })
            .verifyComplete();
    }

    @Test
    void testFindByIdAndStatus() {
        StepVerifier.create(customerOrderRepository.findByIdAndStatus(7, OrderStatus.COMPLETED))
            .expectNextMatches(order -> {
                assertEquals(OrderStatus.COMPLETED, order.getStatus());
                assertEquals(1500.0, order.getCompletedOrderPrice());
                return true;
            })
            .verifyComplete();
    }

    @Test
    void testFindByIdAndStatus_WrongStatus() {
        StepVerifier.create(customerOrderRepository.findByIdAndStatus(6, OrderStatus.COMPLETED))
            .verifyComplete();
    }

    @Test
    void testFindAllByStatus() {
        StepVerifier.create(customerOrderRepository.findAllByStatus(OrderStatus.COMPLETED).collectList())
            .expectNextMatches(orders -> {
                if (orders.size() != 2) return false;
                return orders.stream()
                    .allMatch(order -> order.getStatus() == OrderStatus.COMPLETED);
            })
            .verifyComplete();
    }

    @Test
    void testFindByIdAndStatus_WrongId() {
        StepVerifier.create(customerOrderRepository.findByIdAndStatus(999, OrderStatus.COMPLETED))
            .verifyComplete();
    }
}

