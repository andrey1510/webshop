package com.webshop.repositories;

import com.webshop.entities.CustomerOrder;
import com.webshop.entities.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Sql("/test-data-full.sql")
@ActiveProfiles("test")
public class CustomerOrderRepositoryTest {

    @Autowired
    private CustomerOrderRepository customerOrderRepository;

    @Test
    public void testFindByStatus() {
        Optional<CustomerOrder> foundOrder = customerOrderRepository.findByStatus(OrderStatus.CART);

        assertTrue(foundOrder.isPresent());

        CustomerOrder order = foundOrder.get();
        assertEquals(OrderStatus.CART, order.getStatus());
        assertEquals(0.0, order.getCompletedOrderPrice());
    }

    @Test
    public void testFindAllByStatus() {

        List<CustomerOrder> foundOrders = customerOrderRepository.findAllByStatus(OrderStatus.COMPLETED);
        assertEquals(2, foundOrders.size());

        CustomerOrder firstOrder = foundOrders.get(0);
        assertEquals(OrderStatus.COMPLETED, firstOrder.getStatus());
        assertEquals(1500.0, firstOrder.getCompletedOrderPrice());

        CustomerOrder secondFoundOrder = foundOrders.get(1);
        assertEquals(OrderStatus.COMPLETED, secondFoundOrder.getStatus());
        assertEquals(700.0, secondFoundOrder.getCompletedOrderPrice());
    }

}
