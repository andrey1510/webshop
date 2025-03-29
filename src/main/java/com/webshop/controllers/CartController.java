package com.webshop.controllers;

import com.webshop.entities.CustomerOrder;
import com.webshop.entities.OrderItem;
import com.webshop.exceptions.AlreadyInCartException;
import com.webshop.exceptions.CartIsEmptyException;
import com.webshop.exceptions.WrongQuantityException;
import com.webshop.services.CartService;
import com.webshop.services.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.web.reactive.result.view.RedirectView;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final ProductService productService;

    @GetMapping
    public Mono<String> getCart(ServerWebExchange exchange) {
        Mono<CustomerOrder> cartMono = cartService.getCurrentCart()
            .doOnNext(order -> {
                if (order.getItems() == null) {
                    order.setItems(new ArrayList<>());
                }
            })

            //логирование
            .doOnNext(order -> log.info("Cart contents:\n{}", formatOrderForLog(order)))
            .doOnError(e -> log.error("Error retrieving cart", e));


        Mono<Double> totalPriceMono = cartMono.flatMap(order -> {
            List<OrderItem> items = order.getItems() != null ? order.getItems() : Collections.emptyList();
            return Flux.fromIterable(items)
                .flatMap(item -> productService.getProductById(item.getProductId())
                    .map(product -> product.getPrice() * item.getQuantity()))
                .reduce(0.0, Double::sum);
        });

        return cartMono.zipWith(totalPriceMono)
            .flatMap(tuple -> {
                CustomerOrder order = tuple.getT1();
                Double totalPrice = tuple.getT2();

                exchange.getAttributes().put("cart", order);
                exchange.getAttributes().put("totalPrice", totalPrice);

                return Mono.just("cart");
            });
    }

    private String formatOrderForLog(CustomerOrder order) {
        return String.format(
            "Order ID: %d\nStatus: %s\nItems count: %d\nItems:\n%s",
            order.getId(),
            order.getStatus(),
            order.getItems().size(),
            order.getItems().stream()
                .map(item -> String.format(
                    "  - Item ID: %d, Product ID: %d, Quantity: %d",
                    item.getId(),
                    item.getProductId(),
                    item.getQuantity()))
                .collect(Collectors.joining("\n"))
        );
    }


    @PostMapping(value = "/add")
    public Mono<String> addCartItem(
        @RequestParam("productId") Integer productId,
        @RequestParam(value = "quantity", defaultValue = "1") Integer quantity,
        ServerWebExchange exchange) {

        System.out.println("111111111111111111111111111 addCartItem controller");

        log.info("ADD TO CART ENDPOINT HIT - Product ID: {}, Quantity: {}", productId, quantity);

        return cartService.addItemToCart(productId, quantity)
            .then(Mono.defer(() -> {
                String referer = exchange.getRequest().getHeaders().getFirst("Referer");
                log.info("Redirecting back to: {}", referer);
                return Mono.just("redirect:" + (referer != null ? referer : "/products"));
            }))
            .onErrorResume(e -> {
                log.error("Error adding to cart: {}", e.getMessage());
                return exchange.getSession()
                    .doOnNext(session -> session.getAttributes().put("errorMessage", e.getMessage()))
                    .thenReturn("redirect:" + exchange.getRequest().getHeaders().getFirst("Referer"));
            });
    }


    @PostMapping("/update")
    public Mono<RedirectView> updateCartItem(
        @RequestParam("productId") Integer productId,
        @RequestParam("quantity") Integer quantity,
        @RequestHeader("Referer") String referer) {
        return cartService.updateItemQuantity(productId, quantity).then(Mono.fromCallable(() -> {
            RedirectView redirectView = new RedirectView(referer);
            redirectView.setStatusCode(HttpStatus.FOUND);
            return redirectView;
        }));
    }

    @PostMapping("/remove")
    public Mono<RedirectView> removeCartItem(
        @RequestParam("productId") Integer productId,
        @RequestHeader("Referer") String referer) {
        return cartService.removeCartItem(productId).then(Mono.fromCallable(() -> {
            RedirectView redirectView = new RedirectView(referer);
            redirectView.setStatusCode(HttpStatus.FOUND);
            return redirectView;
        }));
    }
    @PostMapping("/checkout")
    public Mono<RedirectView> completeOrder(ServerWebExchange exchange) {

        Mono<CustomerOrder> checkoutOperation = cartService.completeOrder();

        Mono<RedirectView> successRedirect = checkoutOperation.map(order -> {
            RedirectView redirectView = new RedirectView("/orders/" + order.getId());
            redirectView.setStatusCode(HttpStatus.FOUND);
            return redirectView;
        });

        Mono<RedirectView> errorRedirect = exchange.getSession()
            .doOnNext(session -> session.getAttributes().put("errorMessage", "Корзина пуста"))
            .then(Mono.fromCallable(() -> {
                RedirectView redirectView = new RedirectView("/cart");
                redirectView.setStatusCode(HttpStatus.FOUND);
                return redirectView;
            }));

        return checkoutOperation.flatMap(order -> successRedirect)
            .onErrorResume(CartIsEmptyException.class, e -> errorRedirect);
    }

    @ExceptionHandler({AlreadyInCartException.class, CartIsEmptyException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<String> handleCartExceptions(RuntimeException ex, ServerWebExchange exchange) {
        exchange.getAttributes().put("errorMessage", ex.getMessage());
        return Mono.just("cart");
    }
}
