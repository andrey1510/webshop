package com.shopservice.services;

import com.shopservice.entities.CustomerOrder;
import com.shopservice.entities.OrderItem;
import com.shopservice.entities.OrderStatus;
import com.shopservice.exceptions.CompletedCustomerOrderNotFoundException;
import com.shopservice.repositories.CustomerOrderRepository;
import com.shopservice.repositories.OrderItemProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;
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

    @Mock
    private UserService userService;

    @InjectMocks
    private CustomerOrderServiceImpl customerOrderService;

    private CustomerOrder completedOrder1;
    private CustomerOrder completedOrder2;
    private OrderItem orderItem1;
    private OrderItem orderItem2;
    private static final Integer TEST_USER_ID = 1;

    @BeforeEach
    void setUp() {
        lenient().when(userService.getCurrentUserId()).thenReturn(Mono.just(TEST_USER_ID));

        completedOrder1 = new CustomerOrder();
        completedOrder1.setId(1);
        completedOrder1.setStatus(OrderStatus.COMPLETED);
        completedOrder1.setCompletedOrderPrice(100.0);
        completedOrder1.setUserId(TEST_USER_ID);

        completedOrder2 = new CustomerOrder();
        completedOrder2.setId(2);
        completedOrder2.setStatus(OrderStatus.COMPLETED);
        completedOrder2.setCompletedOrderPrice(200.0);
        completedOrder2.setUserId(TEST_USER_ID);

        orderItem1 = new OrderItem();
        orderItem1.setId(1);
        orderItem1.setCustomerOrderId(1);

        orderItem2 = new OrderItem();
        orderItem2.setId(2);
        orderItem2.setCustomerOrderId(1);
    }

    @Test
    void getCompletedOrderById() {
        when(customerOrderRepository.findByIdAndStatusAndUserId(1, OrderStatus.COMPLETED, TEST_USER_ID))
            .thenReturn(Mono.just(completedOrder1));
        when(orderItemProductRepository.findByCustomerOrderIdWithProduct(1))
            .thenReturn(Flux.just(orderItem1, orderItem2));

        StepVerifier.create(customerOrderService.getCompletedOrderById(1))
            .expectNextMatches(order -> {
                assertEquals(1, order.getId());
                assertEquals(2, order.getItems().size());
                assertEquals(TEST_USER_ID, order.getUserId());
                return true;
            })
            .verifyComplete();
    }

    @Test
    void getCompletedOrderById_OrderNotExists() {
        when(customerOrderRepository.findByIdAndStatusAndUserId(999, OrderStatus.COMPLETED, TEST_USER_ID))
            .thenReturn(Mono.empty());

        StepVerifier.create(customerOrderService.getCompletedOrderById(999))
            .expectError(CompletedCustomerOrderNotFoundException.class)
            .verify();
    }

    @Test
    void getCompletedOrders() {
        when(customerOrderRepository.findAllByStatusAndUserId(OrderStatus.COMPLETED, TEST_USER_ID))
            .thenReturn(Flux.just(completedOrder1, completedOrder2));

        StepVerifier.create(customerOrderService.getCompletedOrders())
            .expectNextMatches(order -> order.getId() == 1)
            .expectNextMatches(order -> order.getId() == 2)
            .verifyComplete();
    }

    @Test
    void getCompletedOrders_NoOrders() {
        when(customerOrderRepository.findAllByStatusAndUserId(OrderStatus.COMPLETED, TEST_USER_ID))
            .thenReturn(Flux.empty());

        StepVerifier.create(customerOrderService.getCompletedOrders())
            .verifyComplete();
    }

    @Test
    void getTotalPriceOfCompletedOrders() {
        when(customerOrderRepository.findAllByStatus(OrderStatus.COMPLETED))
            .thenReturn(Flux.just(completedOrder1, completedOrder2));

        StepVerifier.create(customerOrderService.getTotalPriceOfCompletedOrders())
            .expectNext(300.0)
            .verifyComplete();
    }

    @Test
    void getTotalPriceOfCompletedOrders_NoOrders() {
        when(customerOrderRepository.findAllByStatus(OrderStatus.COMPLETED))
            .thenReturn(Flux.empty());

        StepVerifier.create(customerOrderService.getTotalPriceOfCompletedOrders())
            .expectNext(0.0)
            .verifyComplete();
    }
}