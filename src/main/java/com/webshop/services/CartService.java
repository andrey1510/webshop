package com.webshop.services;

import com.webshop.entities.CustomerOrder;
import com.webshop.entities.OrderItem;
import org.springframework.transaction.annotation.Transactional;

public interface CartService {

    CustomerOrder getCurrentCart();

    CustomerOrder createNewCart();

    Double calculateTotalPrice(CustomerOrder cart);

    void addItemToCart(Integer productId, Integer quantity);

    void updateItemQuantity(Integer productId, Integer quantity);

    void removeCartItem(Integer productId);

    OrderItem findCartItemByProductId(CustomerOrder cart, Integer productId);
}
