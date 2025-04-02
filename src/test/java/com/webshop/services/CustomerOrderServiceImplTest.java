package com.webshop.services;

import com.webshop.entities.CustomerOrder;
import com.webshop.entities.OrderItem;
import com.webshop.entities.OrderStatus;
import com.webshop.exceptions.CompletedCustomerOrderNotFoundException;
import com.webshop.repositories.CustomerOrderRepository;
import com.webshop.repositories.OrderItemProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class CustomerOrderServiceImplTest {

    @Mock
    private CustomerOrderRepository customerOrderRepository;

    @Mock
    private OrderItemProductRepository orderItemProductRepository;

    @InjectMocks
    private CustomerOrderServiceImpl customerOrderService;

    private CustomerOrder completedOrder1;
    private CustomerOrder completedOrder2;
    private OrderItem orderItem1;
    private OrderItem orderItem2;

    @BeforeEach
    void setUp() {
        completedOrder1 = new CustomerOrder();
        completedOrder1.setId(1);
        completedOrder1.setStatus(OrderStatus.COMPLETED);
        completedOrder1.setCompletedOrderPrice(100.0);

        completedOrder2 = new CustomerOrder();
        completedOrder2.setId(2);
        completedOrder2.setStatus(OrderStatus.COMPLETED);
        completedOrder2.setCompletedOrderPrice(200.0);

        orderItem1 = new OrderItem();
        orderItem1.setId(1);
        orderItem1.setCustomerOrderId(1);

        orderItem2 = new OrderItem();
        orderItem2.setId(2);
        orderItem2.setCustomerOrderId(1);
    }

    @Test
    void testGetCompletedOrderById() {
        when(customerOrderRepository.findByIdAndStatus(1, OrderStatus.COMPLETED))
            .thenReturn(Mono.just(completedOrder1));
        when(orderItemProductRepository.findByCustomerOrderIdWithProduct(1))
            .thenReturn(Flux.just(orderItem1, orderItem2));

        StepVerifier.create(customerOrderService.getCompletedOrderById(1))
            .expectNextMatches(order -> {
                assertEquals(1, order.getId());
                assertEquals(2, order.getItems().size());
                return true;
            })
            .verifyComplete();
    }

    @Test
    void testGetCompletedOrderById_OrderNotExists() {
        when(customerOrderRepository.findByIdAndStatus(999, OrderStatus.COMPLETED))
            .thenReturn(Mono.empty());

        StepVerifier.create(customerOrderService.getCompletedOrderById(999))
            .expectError(CompletedCustomerOrderNotFoundException.class)
            .verify();
    }

    @Test
    void testGetCompletedOrders() {
        when(customerOrderRepository.findAllByStatus(OrderStatus.COMPLETED))
            .thenReturn(Flux.just(completedOrder1, completedOrder2));

        StepVerifier.create(customerOrderService.getCompletedOrders())
            .expectNext(completedOrder1)
            .expectNext(completedOrder2)
            .verifyComplete();
    }

    @Test
    void testGetCompletedOrders_NoOrders() {
        when(customerOrderRepository.findAllByStatus(OrderStatus.COMPLETED))
            .thenReturn(Flux.empty());

        StepVerifier.create(customerOrderService.getCompletedOrders())
            .verifyComplete();
    }

    @Test
    void testGetTotalPriceOfCompletedOrders() {
        when(customerOrderRepository.findAllByStatus(OrderStatus.COMPLETED))
            .thenReturn(Flux.just(completedOrder1, completedOrder2));

        StepVerifier.create(customerOrderService.getTotalPriceOfCompletedOrders())
            .expectNext(300.0)
            .verifyComplete();
    }

    @Test
    void testGetTotalPriceOfCompletedOrders_NoOrders() {
        when(customerOrderRepository.findAllByStatus(OrderStatus.COMPLETED))
            .thenReturn(Flux.empty());

        StepVerifier.create(customerOrderService.getTotalPriceOfCompletedOrders())
            .expectNext(0.0)
            .verifyComplete();
    }
}