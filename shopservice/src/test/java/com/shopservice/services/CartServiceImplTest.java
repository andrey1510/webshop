package com.shopservice.services;

import com.shopservice.entities.CustomerOrder;
import com.shopservice.entities.OrderItem;
import com.shopservice.entities.OrderStatus;
import com.shopservice.entities.Product;
import com.shopservice.exceptions.WrongQuantityException;
import com.shopservice.repositories.CustomerOrderRepository;
import com.shopservice.repositories.OrderItemProductRepository;
import com.shopservice.repositories.OrderItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CustomerOrderRepository customerOrderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private OrderItemProductRepository orderItemProductRepository;

    @Mock
    private ProductService productService;

    @Mock
    private UserService userService;

    @InjectMocks
    private CartServiceImpl cartService;

    private CustomerOrder cartOrder;
    private CustomerOrder completedOrder;
    private OrderItem cartItem1;
    private OrderItem cartItem2;
    private Product product1;
    private Product product2;
    private static final Integer TEST_USER_ID = 1;

    @BeforeEach
    void setUp() {
        lenient().when(userService.getCurrentUserId()).thenReturn(Mono.just(TEST_USER_ID));

        cartOrder = CustomerOrder.builder()
            .id(1)
            .userId(TEST_USER_ID)
            .status(OrderStatus.CART)
            .items(new ArrayList<>())
            .build();
        completedOrder = CustomerOrder.builder()
            .id(2)
            .userId(TEST_USER_ID)
            .status(OrderStatus.COMPLETED)
            .timestamp(LocalDateTime.now())
            .completedOrderPrice(300.0)
            .items(new ArrayList<>())
            .build();
        product1 = Product.builder()
            .id(1)
            .price(100.0)
            .build();
        product2 = Product.builder()
            .id(2)
            .price(200.0)
            .build();
        cartItem1 = OrderItem.builder()
            .id(1)
            .customerOrderId(1)
            .productId(1)
            .quantity(1)
            .product(product1)
            .build();
        cartItem2 = OrderItem.builder()
            .id(2)
            .customerOrderId(1)
            .productId(2)
            .quantity(2)
            .product(product2)
            .build();
        cartOrder.getItems().add(cartItem1);
        cartOrder.getItems().add(cartItem2);
    }

    @Test
    void getCurrentCartWithProducts_CreateNewCart() {
        CustomerOrder newCart = CustomerOrder.builder()
            .id(3)
            .userId(TEST_USER_ID)
            .status(OrderStatus.CART)
            .items(new ArrayList<>())
            .build();

        when(customerOrderRepository.findByStatusAndUserId(OrderStatus.CART, TEST_USER_ID))
            .thenReturn(Mono.empty());

        when(customerOrderRepository.save(argThat(order ->
            order.getStatus() == OrderStatus.CART && order.getUserId().equals(TEST_USER_ID))))
            .thenReturn(Mono.just(newCart));

        when(orderItemProductRepository.findByCustomerOrderIdWithProduct(3))
            .thenReturn(Flux.empty());

        StepVerifier.create(cartService.getCurrentCartWithProducts())
            .expectNextMatches(order -> {
                assertEquals(OrderStatus.CART, order.getStatus());
                assertTrue(order.getItems().isEmpty());
                assertEquals(TEST_USER_ID, order.getUserId());
                return true;
            })
            .verifyComplete();

        verify(customerOrderRepository).findByStatusAndUserId(OrderStatus.CART, TEST_USER_ID);
        verify(customerOrderRepository).save(argThat(order ->
            order.getStatus() == OrderStatus.CART && order.getUserId().equals(TEST_USER_ID)));
    }

    @Test
    void completeOrder() {
        when(customerOrderRepository.findByStatusAndUserId(OrderStatus.CART, TEST_USER_ID))
            .thenReturn(Mono.just(cartOrder));
        when(orderItemProductRepository.findByCustomerOrderIdWithProduct(1))
            .thenReturn(Flux.just(cartItem1, cartItem2));
        when(customerOrderRepository.save(any(CustomerOrder.class)))
            .thenReturn(Mono.just(completedOrder));

        StepVerifier.create(cartService.completeOrder())
            .expectNextMatches(order -> {
                assertEquals(OrderStatus.COMPLETED, order.getStatus());
                assertNotNull(order.getTimestamp());
                assertEquals(500.0, order.getCompletedOrderPrice());
                return true;
            })
            .verifyComplete();
    }

    @Test
    void addItemToCart() {
        Product newProduct = Product.builder().id(3).price(150.0).build();
        OrderItem newItem = OrderItem.builder()
            .id(3)
            .customerOrderId(1)
            .productId(3)
            .quantity(1)
            .product(newProduct)
            .build();

        when(customerOrderRepository.findByStatusAndUserId(OrderStatus.CART, TEST_USER_ID))
            .thenReturn(Mono.just(cartOrder));
        when(productService.getProductById(3))
            .thenReturn(Mono.just(newProduct));
        when(orderItemRepository.save(any(OrderItem.class)))
            .thenReturn(Mono.just(newItem));
        when(orderItemRepository.findByCustomerOrderId(1))
            .thenReturn(Flux.just(cartItem1, cartItem2));
        when(customerOrderRepository.save(any(CustomerOrder.class)))
            .thenReturn(Mono.just(cartOrder));

        StepVerifier.create(cartService.addItemToCart(3, 1))
            .verifyComplete();

        verify(orderItemRepository).save(any(OrderItem.class));
    }

    @Test
    void updateItemQuantity_WithInvalidQuantity() {

        lenient().when(userService.getCurrentUserId()).thenReturn(Mono.just(TEST_USER_ID));

        lenient().when(customerOrderRepository.findByStatusAndUserId(OrderStatus.CART, TEST_USER_ID))
            .thenReturn(Mono.just(cartOrder));
        lenient().when(orderItemRepository.findByCustomerOrderId(cartOrder.getId()))
            .thenReturn(Flux.just(cartItem1, cartItem2));

        StepVerifier.create(cartService.updateItemQuantity(1, 0))
            .expectErrorMatches(throwable ->
                throwable instanceof WrongQuantityException &&
                    throwable.getMessage().equals("Количество должно быть больше 0"))
            .verify();
    }

    @Test
    void removeCartItem() {
        when(customerOrderRepository.findByStatusAndUserId(OrderStatus.CART, TEST_USER_ID))
            .thenReturn(Mono.just(cartOrder));
        when(orderItemRepository.findByCustomerOrderId(1))
            .thenReturn(Flux.just(cartItem1, cartItem2));
        when(orderItemRepository.delete(any(OrderItem.class)))
            .thenReturn(Mono.empty());
        when(customerOrderRepository.save(any(CustomerOrder.class)))
            .thenReturn(Mono.just(cartOrder));

        StepVerifier.create(cartService.removeCartItem(1))
            .verifyComplete();

        verify(orderItemRepository).delete(argThat(item -> item.getId() == 1));
    }

    @Test
    void cartItemByProductId() {

        cartOrder.getItems().add(cartItem1);
        cartOrder.getItems().add(cartItem2);

        StepVerifier.create(cartService.findCartItemByProductId(cartOrder, 1))
            .expectNextMatches(item ->
                item != null &&
                    item.getProductId().equals(1) &&
                    item.getId().equals(1))
            .verifyComplete();

        StepVerifier.create(cartService.findCartItemByProductId(cartOrder, 999))
            .expectNextCount(0)
            .verifyComplete();
    }

}