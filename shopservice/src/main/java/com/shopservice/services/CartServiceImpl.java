package com.shopservice.services;

import com.shopservice.entities.CustomerOrder;
import com.shopservice.entities.OrderItem;
import com.shopservice.entities.OrderStatus;
import com.shopservice.exceptions.AlreadyInCartException;
import com.shopservice.exceptions.CartIsEmptyException;
import com.shopservice.exceptions.ItemIsNotInCartException;
import com.shopservice.exceptions.WrongQuantityException;
import com.shopservice.repositories.CustomerOrderRepository;
import com.shopservice.repositories.OrderItemProductRepository;
import com.shopservice.repositories.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
@CacheConfig(cacheNames = "carts")
public class CartServiceImpl implements CartService{

    private final CustomerOrderRepository customerOrderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderItemProductRepository orderItemProductRepository;

    private final ProductService productService;

    @Override
    @Cacheable(key = "'user:' + #result?.id", unless = "#result?.status != T(com.shopservice.entities.OrderStatus).CART")
    public Mono<CustomerOrder> getCurrentCartWithProducts() {
        return customerOrderRepository.findByStatus(OrderStatus.CART)
            .switchIfEmpty(createNewCart())
            .flatMap(order ->
                orderItemProductRepository.findByCustomerOrderIdWithProduct(order.getId())
                    .collectList()
                    .doOnNext(order::setItems)
                    .thenReturn(order)
            );
    }

    @Override
    public Mono<CustomerOrder> getCurrentCartNoProducts() {
        return customerOrderRepository.findByStatus(OrderStatus.CART)
            .switchIfEmpty(createNewCart())
            .flatMap(order -> orderItemRepository.findByCustomerOrderId(order.getId())
                .collectList()
                .doOnNext(items -> order.setItems(items != null ? items : new ArrayList<>()))
                .thenReturn(order));
    }

    @Override
    public Mono<Map<Integer, Integer>> getCartProductsQuantity() {
        return getCurrentCartWithProducts()
            .flatMapMany(order -> Flux.fromIterable(order.getItems() != null ? order.getItems() : Collections.emptyList()))
            .filter(item -> item.getProductId() != null)
            .collectMap(
                OrderItem::getProductId,
                OrderItem::getQuantity
            )
            .defaultIfEmpty(Collections.emptyMap());
    }

    @Override
    @CacheEvict(key = "'user:' + #result?.id")
    public Mono<CustomerOrder> createNewCart() {
        CustomerOrder orderInCart = new CustomerOrder();
        orderInCart.setStatus(OrderStatus.CART);
        orderInCart.setItems(new ArrayList<>());
        return customerOrderRepository.save(orderInCart);
    }

    @Override
    @CacheEvict(key = "'user:' + #result?.id", beforeInvocation = true)
    public Mono<CustomerOrder> completeOrder() {
        return getCurrentCartWithProducts()
            .flatMap(orderInCart -> {
                if (orderInCart.getItems().isEmpty()) return Mono.error(new CartIsEmptyException("Корзина пустая."));

                orderInCart.setStatus(OrderStatus.COMPLETED);
                orderInCart.setTimestamp(LocalDateTime.now());
                orderInCart.setCompletedOrderPrice(orderInCart.getItems().stream()
                    .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                    .sum());

                return customerOrderRepository.save(orderInCart)
                    .then(createNewCart())
                    .thenReturn(orderInCart);
            });
    }

    @Override
    @CacheEvict(key = "'user:' + #result?.id")
    public Mono<Void> addItemToCart(Integer productId, Integer quantity) {

        if (quantity <= 0) return Mono.error(new WrongQuantityException("Количество должно быть больше 0"));

        return getCurrentCartNoProducts()
            .flatMap(order -> findCartItemByProductId(order, productId)
                .flatMap(item -> Mono.error(new AlreadyInCartException("Товар уже в корзине")))
                .switchIfEmpty(productService.getProductById(productId)
                    .flatMap(product -> {
                        OrderItem newItem = OrderItem.builder()
                            .customerOrderId(order.getId())
                            .productId(product.getId())
                            .quantity(quantity)
                            .build();
                        log.info("Saving new order item: {}", newItem);
                        return orderItemRepository.save(newItem)
                            .doOnNext(savedItem -> log.info("Saved order item: {}", savedItem))
                            .then(customerOrderRepository.save(order))
                            .doOnNext(savedOrder -> log.info("Updated cart: {}", savedOrder));
                    })
                )
            )
            .then()
            .doOnError(e -> log.error("Failed to add item to cart: {}", e.getMessage(), e));
    }

    @Override
    @CacheEvict(key = "'user:' + #result?.id")
    public Mono<Void> updateItemQuantity(Integer productId, Integer quantity) {

        if (quantity <= 0) return Mono.error(new WrongQuantityException("Количество должно быть больше 0"));

        return getCurrentCartNoProducts()
            .flatMap(order -> findCartItemByProductId(order, productId))
            .switchIfEmpty(Mono.defer(() -> Mono.error(new ItemIsNotInCartException("Товар не найден в корзине."))))
            .flatMap(item -> {
                item.setQuantity(quantity);
                return orderItemRepository.save(item)
                    .doOnError(e -> log.error("Failed to update item quantity", e))
                    .then();
            });
    }

    @Override
    @CacheEvict(key = "'user:' + #result?.id")
    public Mono<Void> removeCartItem(Integer productId) {
        return getCurrentCartNoProducts()
            .flatMap(orderInCart -> findCartItemByProductId(orderInCart, productId)
                .flatMap(existingItem -> {
                    orderInCart.getItems().remove(existingItem);
                    return orderItemRepository.delete(existingItem)
                        .then(customerOrderRepository.save(orderInCart));
                }))
            .then();
    }

    @Override
    public Mono<OrderItem> findCartItemByProductId(CustomerOrder orderInCart, Integer productId) {
        return Flux.fromIterable(orderInCart.getItems() != null ? orderInCart.getItems() : Collections.emptyList())
            .filter(item -> item.getProductId() != null && item.getProductId().equals(productId))
            .next();
    }
}

