package com.webshop.services;

import com.webshop.entities.CustomerOrder;
import com.webshop.entities.OrderItem;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface CartService {

    Mono<CustomerOrder> getCurrentCart();

    Mono<Map<Integer, Integer>> getCartProductsQuantity();

    Mono<CustomerOrder> createNewCart();

    Mono<CustomerOrder> completeOrder();

    Mono<Void> addItemToCart(Integer productId, Integer quantity);

    Mono<Void> updateItemQuantity(Integer productId, Integer quantity);

    Mono<Void> removeCartItem(Integer productId);

    Mono<OrderItem> findCartItemByProductId(CustomerOrder cart, Integer productId);
}
