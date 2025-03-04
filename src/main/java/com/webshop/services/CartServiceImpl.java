package com.webshop.services;

import com.webshop.entities.CustomerOrder;
import com.webshop.entities.OrderItem;
import com.webshop.entities.OrderStatus;
import com.webshop.entities.Product;
import com.webshop.repositories.CustomerOrderRepository;
import com.webshop.repositories.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;

@RequiredArgsConstructor
@Service
public class CartServiceImpl implements CartService{

    private final CustomerOrderRepository customerOrderRepository;
    private final OrderItemRepository orderItemRepository;

    private final ProductService productService;

    @Override
    @Transactional
    public CustomerOrder getCurrentCart() {
        return customerOrderRepository.findByStatus(OrderStatus.CART)
            .orElseGet(this::createNewCart);
    }

    @Override
    @Transactional
    public CustomerOrder createNewCart() {
        CustomerOrder cart = new CustomerOrder();
        cart.setStatus(OrderStatus.CART);
        cart.setItems(new HashSet<>());
        return customerOrderRepository.save(cart);
    }

    @Transactional
    @Override
    public CustomerOrder completeOrder() {

        CustomerOrder currentCart = getCurrentCart();

        //ToDo
        if (currentCart.getItems().isEmpty()) throw new IllegalStateException("Корзина пустая.");

        currentCart.setStatus(OrderStatus.COMPLETED);
        currentCart.setTimestamp(LocalDateTime.now());
        currentCart.setCompletedOrderPrice(calculateTotalPrice(currentCart));

        customerOrderRepository.save(currentCart);

        createNewCart();

        return currentCart;
    }

    @Override
    public Double calculateTotalPrice(CustomerOrder cart) {
        return cart.getItems().stream()
            .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
            .sum();
    }

    @Transactional
    @Override
    public void addItemToCart(Integer productId, Integer quantity) {
        //ToDo
        if (quantity <= 0) throw new IllegalArgumentException("Количество должно быть больше 0");

        CustomerOrder cart = getCurrentCart();

        //ToDo
        if (findCartItemByProductId(cart, productId) != null) throw new IllegalStateException("Товар уже добавлен.");

        Product product = productService.getProductById(productId);

        OrderItem newItem = new OrderItem();
        newItem.setCustomerOrder(cart);
        newItem.setProduct(product);
        newItem.setQuantity(quantity);
        orderItemRepository.save(newItem);
        cart.getItems().add(newItem);

        customerOrderRepository.save(cart);
    }

    @Transactional
    @Override
    public void updateItemQuantity(Integer productId, Integer quantity) {
        //ToDo
        if (quantity <= 0) throw new IllegalArgumentException("Количество должно быть больше 0");

        CustomerOrder cart = getCurrentCart();

        OrderItem existingItem = findCartItemByProductId(cart, productId);

        //ToDo
        if (existingItem == null) throw new IllegalStateException("Товар не найден в корзине.");

        existingItem.setQuantity(quantity);
        customerOrderRepository.save(cart);
    }

    @Transactional
    @Override
    public void removeCartItem(Integer productId) {
        CustomerOrder cart = getCurrentCart();

        OrderItem existingItem = findCartItemByProductId(cart, productId);

        if (existingItem != null) {
            cart.getItems().remove(existingItem);
            orderItemRepository.delete(existingItem);
        }

        customerOrderRepository.save(cart);
    }

    @Transactional
    @Override
    public OrderItem findCartItemByProductId(CustomerOrder cart, Integer productId) {
        return cart.getItems().stream()
            .filter(item -> item.getProduct().getId().equals(productId))
            .findFirst()
            .orElse(null);
    }


}

