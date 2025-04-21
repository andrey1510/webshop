package com.shopservice.controllers;

import com.shopservice.entities.CustomerOrder;
import com.shopservice.entities.OrderStatus;
import com.shopservice.exceptions.CompletedCustomerOrderNotFoundException;
import com.shopservice.services.CustomerOrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private CustomerOrderService customerOrderService;

    @InjectMocks
    private OrderController orderController;

    private CustomerOrder testOrder;
    private List<CustomerOrder> testOrders;

    @BeforeEach
    void setUp() {
        testOrder = CustomerOrder.builder()
            .id(1)
            .userId(123)
            .status(OrderStatus.COMPLETED)
            .timestamp(LocalDateTime.now())
            .completedOrderPrice(150.0)
            .build();

        testOrders = List.of(
            testOrder,
            CustomerOrder.builder()
                .id(2)
                .userId(123)
                .status(OrderStatus.COMPLETED)
                .timestamp(LocalDateTime.now().minusDays(1))
                .completedOrderPrice(200.0)
                .build()
        );
    }

    @Test
    @WithMockUser(roles = "USER")
    void getCompletedOrder() {
        when(customerOrderService.getCompletedOrderById(anyInt()))
            .thenReturn(Mono.just(testOrder));

        Mono<Rendering> result = orderController.getCompletedOrder(1);

        StepVerifier.create(result)
            .assertNext(rendering -> {
                assertEquals("order", rendering.view());
                assertNotNull(rendering.modelAttributes().get("order"));
                assertEquals(testOrder, rendering.modelAttributes().get("order"));
            })
            .verifyComplete();
    }

    @Test
    @WithMockUser(roles = "USER")
    void getCompletedOrder_WithNonExistingId() {
        when(customerOrderService.getCompletedOrderById(anyInt()))
            .thenReturn(Mono.empty());

        Mono<Rendering> result = orderController.getCompletedOrder(999);

        StepVerifier.create(result)
            .expectErrorMatches(ex -> ex instanceof CompletedCustomerOrderNotFoundException)
            .verify();
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllCompletedOrders() {
        when(customerOrderService.getCompletedOrders())
            .thenReturn(Flux.fromIterable(testOrders));
        when(customerOrderService.getTotalPriceOfCompletedOrders())
            .thenReturn(Mono.just(350.0));

        Mono<Rendering> result = orderController.getAllCompletedOrders();

        StepVerifier.create(result)
            .assertNext(rendering -> {
                assertEquals("orders", rendering.view());
                assertEquals(testOrders, rendering.modelAttributes().get("orders"));
                assertEquals(350.0, rendering.modelAttributes().get("totalPrice"));
            })
            .verifyComplete();
    }

}
