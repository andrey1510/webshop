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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class CartServiceImpl implements CartService{

    private final CustomerOrderRepository customerOrderRepository;
    private final OrderItemRepository orderItemRepository;

    private final ProductService productService;

    @Override
    public Mono<CustomerOrder> getCurrentCart() {
        return customerOrderRepository.findByStatus(OrderStatus.CART)
            .switchIfEmpty(createNewCart());
    }

    public Mono<Map<Integer, Integer>> getCartProductsQuantity() {
        return getCurrentCart()
            .flatMapMany(order -> {
                List<OrderItem> items = order.getItems();
                return items != null ? Flux.fromIterable(items) : Flux.empty();
            })
            .filter(item -> item.getProduct() != null && item.getProduct().getId() != null)
            .collectMap(
                item -> item.getProduct().getId(),
                OrderItem::getQuantity
            )
            .defaultIfEmpty(Collections.emptyMap());
    }

    @Override
    public Mono<CustomerOrder> createNewCart() {
        CustomerOrder orderInCart = new CustomerOrder();
        orderInCart.setStatus(OrderStatus.CART);
        orderInCart.setItems(new ArrayList<>());
        return customerOrderRepository.save(orderInCart);
    }

    @Override
    public Mono<CustomerOrder> completeOrder() {
        return getCurrentCart()
            .flatMap(orderInCart -> {
                if (orderInCart.getItems().isEmpty()) {
                    return Mono.error(new CartIsEmptyException("Корзина пустая."));
                }

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
    public Mono<Void> addItemToCart(Integer productId, Integer quantity) {
        if (quantity <= 0) {
            return Mono.error(new WrongQuantityException("Количество должно быть больше 0"));
        }

        Mono<CustomerOrder> orderMono = getCurrentCart().cache();

        Mono<OrderItem> existingItemMono = orderMono
            .flatMap(order -> findCartItemByProductId(order, productId));

        Mono<Void> checkDuplicate = existingItemMono
            .flatMap(item -> Mono.error(new AlreadyInCartException("Товар уже добавлен в корзину")));

        Mono<Void> addNewItem = orderMono
            .zipWith(productService.getProductById(productId))
            .flatMap(tuple -> {
                CustomerOrder order = tuple.getT1();
                Product product = tuple.getT2();

                OrderItem newItem = OrderItem.builder()
                    .customerOrder(order)
                    .product(product)
                    .quantity(quantity)
                    .build();

                return orderItemRepository.save(newItem)
                    .doOnNext(savedItem -> order.getItems().add(savedItem))
                    .then(customerOrderRepository.save(order));
            }).then();


        return checkDuplicate
            .switchIfEmpty(addNewItem)
            .then();
    }

    @Override
    public Mono<Void> updateItemQuantity(Integer productId, Integer quantity) {

        if (quantity <= 0) {
            return Mono.error(new WrongQuantityException("Количество должно быть больше 0"));
        }

        Mono<CustomerOrder> orderMono = getCurrentCart().cache();

        Mono<OrderItem> existingItemMono = orderMono
            .flatMap(order -> findCartItemByProductId(order, productId))
            .switchIfEmpty(Mono.error(new ItemIsNotInCartException("Товар не найден в корзине.")));

        Mono<Void> updateOperation = existingItemMono
            .zipWith(orderMono)
            .flatMap(tuple -> {
                OrderItem item = tuple.getT1();
                CustomerOrder order = tuple.getT2();

                item.setQuantity(quantity);
                return customerOrderRepository.save(order);
            }).then();

        return updateOperation.then();
    }


    @Override
    public Mono<Void> removeCartItem(Integer productId) {
        return getCurrentCart()
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
        return Flux.fromIterable(orderInCart.getItems())
            .filter(item -> item.getProduct().getId().equals(productId))
            .next();
    }

}

