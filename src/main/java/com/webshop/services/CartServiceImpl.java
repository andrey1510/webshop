package com.webshop.services;

import com.webshop.entities.CustomerOrder;
import com.webshop.entities.OrderItem;
import com.webshop.entities.OrderStatus;
import com.webshop.entities.Product;
import com.webshop.exceptions.AlreadyInCartException;
import com.webshop.exceptions.CartIsEmptyException;
import com.webshop.exceptions.ItemIsNotInCartException;
import com.webshop.exceptions.WrongQuantityException;
import com.webshop.repositories.CustomerOrderRepository;
import com.webshop.repositories.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Transactional
    @Override
    public Map<Integer, Integer> getCartProductsQuantity() {
        CustomerOrder orderInCart = getCurrentCart();
        return orderInCart.getItems().stream()
            .collect(Collectors.toMap(
                item -> item.getProduct().getId(),
                OrderItem::getQuantity
            ));
    }

    @Override
    @Transactional
    public CustomerOrder createNewCart() {
        CustomerOrder orderInCart = new CustomerOrder();
        orderInCart.setStatus(OrderStatus.CART);
        orderInCart.setItems(new ArrayList<>());
        return customerOrderRepository.save(orderInCart);
    }

    @Transactional
    @Override
    public CustomerOrder completeOrder() {

        CustomerOrder orderInCart = getCurrentCart();

        if (orderInCart.getItems().isEmpty()) throw new CartIsEmptyException("Корзина пустая.");

        orderInCart.setStatus(OrderStatus.COMPLETED);
        orderInCart.setTimestamp(LocalDateTime.now());
        orderInCart.setCompletedOrderPrice(calculateTotalPrice(orderInCart));

        customerOrderRepository.save(orderInCart);

        createNewCart();

        return orderInCart;
    }

    @Override
    public Double calculateTotalPrice(CustomerOrder orderInCart) {
        return orderInCart.getItems().stream()
            .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
            .sum();
    }

    @Transactional
    @Override
    public void addItemToCart(Integer productId, Integer quantity) {

        if (quantity <= 0) throw new WrongThreadException("Количество должно быть больше 0");

        CustomerOrder orderInCart = getCurrentCart();

        if (findCartItemByProductId(orderInCart, productId) != null)
            throw new AlreadyInCartException("Товар уже добавлен в корзину.");

        Product product = productService.getProductById(productId);

        OrderItem newItem = OrderItem.builder()
            .customerOrder(orderInCart)
            .product(product)
            .quantity(quantity)
            .build();
        orderItemRepository.save(newItem);

        orderInCart.getItems().add(newItem);

        customerOrderRepository.save(orderInCart);
    }

    @Transactional
    @Override
    public void updateItemQuantity(Integer productId, Integer quantity) {

        if (quantity <= 0) throw new WrongQuantityException("Количество должно быть больше 0");

        CustomerOrder orderInCart = getCurrentCart();

        OrderItem existingItem = findCartItemByProductId(orderInCart, productId);

        if (existingItem == null) throw new ItemIsNotInCartException("Товар не найден в корзине.");

        existingItem.setQuantity(quantity);
        customerOrderRepository.save(orderInCart);
    }

    @Transactional
    @Override
    public void removeCartItem(Integer productId) {
        CustomerOrder orderInCart = getCurrentCart();

        OrderItem existingItem = findCartItemByProductId(orderInCart, productId);

        if (existingItem != null) {
            orderInCart.getItems().remove(existingItem);
            orderItemRepository.delete(existingItem);
        }

        customerOrderRepository.save(orderInCart);
    }

    @Transactional
    @Override
    public OrderItem findCartItemByProductId(CustomerOrder orderInCart, Integer productId) {
        return orderInCart.getItems().stream()
            .filter(item -> item.getProduct().getId().equals(productId))
            .findFirst()
            .orElse(null);
    }


}

