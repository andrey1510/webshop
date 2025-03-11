package com.webshop.repositories;

import com.webshop.entities.OrderItem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

@DataJpaTest
@Sql("/test-data-full.sql")
public class OrderItemRepositoryTest {

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Test
    public void testDeleteOrderItem() {

        Optional<OrderItem> existingItemOptional = orderItemRepository.findById(6);
        assertTrue(existingItemOptional.isPresent());

        OrderItem existingItem = existingItemOptional.get();
        orderItemRepository.delete(existingItem);

        Optional<OrderItem> deletedItemOptional = orderItemRepository.findById(6);
        assertFalse(deletedItemOptional.isPresent());
    }
}
