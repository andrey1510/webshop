package com.shopservice.controllers;

import com.shopservice.entities.CustomerOrder;
import com.shopservice.entities.OrderItem;
import com.shopservice.entities.OrderStatus;
import com.shopservice.entities.Product;
import com.shopservice.services.CartService;
import com.shopservice.services.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.web.reactive.result.view.RedirectView;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    @Mock
    private CartService cartService;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private CartController cartController;

    private CustomerOrder testOrder;
    private Product testProduct;
    private ServerWebExchange exchange;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
            .id(1)
            .title("Test Product")
            .price(100.0)
            .imagePath("test.jpg")
            .build();

        OrderItem item = new OrderItem();
        item.setProductId(testProduct.getId());
        item.setQuantity(2);
        item.setProduct(testProduct);

        testOrder = CustomerOrder.builder()
            .id(1)
            .userId(123)
            .status(OrderStatus.CART)
            .timestamp(LocalDateTime.now())
            .items(new ArrayList<>(List.of(item)))
            .build();

        exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/cart"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getCart() {
        when(cartService.getCurrentCartWithProducts()).thenReturn(Mono.just(testOrder));
        when(paymentService.checkFunds(anyDouble())).thenReturn(Mono.just(true));

        Mono<String> result = cartController.getCart(exchange);

        StepVerifier.create(result)
            .expectNext("cart")
            .verifyComplete();

        assertEquals(200.0, exchange.getAttributes().get("totalPrice"));
        assertEquals(true, exchange.getAttributes().get("isBalanceSufficient"));
        assertSame(testOrder, exchange.getAttributes().get("cart"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void addCartItem() {
        when(cartService.addItemToCart(anyInt(), anyInt())).thenReturn(Mono.empty());
        ServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.post("/cart/add")
                .header("Referer", "/products/1")
        );

        Mono<String> result = cartController.addCartItem(1, 1, exchange);

        StepVerifier.create(result)
            .expectNext("redirect:/products/1")
            .verifyComplete();
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateCartItem() {
        when(cartService.updateItemQuantity(anyInt(), anyInt())).thenReturn(Mono.empty());

        Mono<RedirectView> result = cartController.updateCartItem(1, 3, "/cart");

        StepVerifier.create(result)
            .assertNext(redirectView -> {
                assertEquals("/cart", redirectView.getUrl());
                assertEquals(HttpStatus.FOUND, redirectView.getStatusCode());
            })
            .verifyComplete();
    }

    @Test
    @WithMockUser(roles = "USER")
    void removeCartItem() {
        when(cartService.removeCartItem(anyInt())).thenReturn(Mono.empty());

        Mono<RedirectView> result = cartController.removeCartItem(1, "/cart");

        StepVerifier.create(result)
            .assertNext(redirectView -> {
                assertEquals("/cart", redirectView.getUrl());
                assertEquals(HttpStatus.FOUND, redirectView.getStatusCode());
            })
            .verifyComplete();
    }

    @Test
    @WithMockUser(roles = "USER")
    void completeOrder() {
        testOrder.setStatus(OrderStatus.COMPLETED);
        when(cartService.getCurrentCartWithProducts()).thenReturn(Mono.just(testOrder));
        when(paymentService.processPayment(anyDouble())).thenReturn(Mono.just(true));
        when(cartService.completeOrder()).thenReturn(Mono.just(testOrder));

        Mono<String> result = cartController.completeOrder(exchange);

        StepVerifier.create(result)
            .expectNextMatches(s -> s.startsWith("redirect:/orders/"))
            .verifyComplete();
    }

    @Test
    @WithMockUser(roles = "USER")
    void completeOrder_WhenPaymentFails() {
        when(cartService.getCurrentCartWithProducts()).thenReturn(Mono.just(testOrder));
        when(paymentService.processPayment(anyDouble())).thenReturn(Mono.just(false));

        Mono<String> result = cartController.completeOrder(exchange);

        StepVerifier.create(result)
            .expectNext("redirect:/cart")
            .verifyComplete();
    }

}