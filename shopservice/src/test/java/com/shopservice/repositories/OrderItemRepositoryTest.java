package com.shopservice.repositories;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import reactor.test.StepVerifier;

import java.util.List;

@DataR2dbcTest
@ActiveProfiles("test")
@SpringJUnitConfig
class OrderItemRepositoryTest {

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Test
    void testFindByCustomerOrderId() {
        StepVerifier.create(orderItemRepository.findByCustomerOrderId(6).collectList())
            .expectNextMatches(items -> {
                if (items.size() != 2) return false;
                return items.stream()
                    .allMatch(item -> item.getCustomerOrderId().equals(6));
            })
            .verifyComplete();
    }

    @Test
    void testFindByCustomerOrderId_Empty() {
        StepVerifier.create(orderItemRepository.findByCustomerOrderId(999).collectList())
            .expectNextMatches(List::isEmpty)
            .verifyComplete();
    }
}
