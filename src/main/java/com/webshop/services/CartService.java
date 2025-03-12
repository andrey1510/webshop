package com.webshop.services;

import com.webshop.entities.CustomerOrder;
import com.webshop.entities.OrderItem;

import java.util.Map;

public interface CartService {

    CustomerOrder getCurrentCart();

    Map<Integer, Integer> getCartProductsQuantity();

    CustomerOrder createNewCart();

    CustomerOrder completeOrder();

    Double calculateTotalPrice(CustomerOrder cart);

    void addItemToCart(Integer productId, Integer quantity);

    void updateItemQuantity(Integer productId, Integer quantity);

    void removeCartItem(Integer productId);

    OrderItem findCartItemByProductId(CustomerOrder cart, Integer productId);
}
