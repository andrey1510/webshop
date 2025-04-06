package com.shopservice.controllers;

import com.shopservice.exceptions.AlreadyInCartException;
import com.shopservice.exceptions.CartIsEmptyException;
import com.shopservice.services.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.springframework.web.reactive.result.view.RedirectView;
import org.springframework.web.bind.annotation.RequestHeader;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    @GetMapping
    public Mono<String> getCart(ServerWebExchange exchange) {
        return cartService.getCurrentCartWithProducts()
            .doOnError(e -> log.error("Error retrieving cart", e))
            .flatMap(order -> {
                double totalPrice = order.getItems().stream()
                    .filter(item -> item.getProduct() != null)
                    .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                    .sum();

                exchange.getAttributes().put("cart", order);
                exchange.getAttributes().put("totalPrice", totalPrice);
                return Mono.just("cart");
            });
    }

    @PostMapping(value = "/add")
    public Mono<String> addCartItem(
        @RequestParam("productId") Integer productId,
        @RequestParam(value = "quantity", defaultValue = "1") Integer quantity,
        ServerWebExchange exchange) {
        return cartService.addItemToCart(productId, quantity)
            .then(Mono.defer(() -> {
                String referer = exchange.getRequest().getHeaders().getFirst("Referer");
                return Mono.just("redirect:" + (referer != null ? referer : "/products"));
            }))
            .onErrorResume(e -> {
                log.error("Error adding item to cart: {}", e.getMessage());
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
        return cartService.updateItemQuantity(productId, quantity)
            .doOnError(e -> log.error("Failed to update: {}, Error: {}", productId, e.getMessage()))
            .then(Mono.fromCallable(() -> {
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
    public Mono<String> completeOrder(ServerWebExchange exchange) {
        return cartService.completeOrder()
            .map(order -> "redirect:/orders/" + order.getId())
            .onErrorResume(CartIsEmptyException.class, e -> exchange.getSession()
                .doOnNext(session -> {
                    session.getAttributes().put("errorMessage", e.getMessage());
                    log.warn("Cart checkout error: {}", e.getMessage());
                })
                .thenReturn("redirect:/cart"));
    }

    @ExceptionHandler({AlreadyInCartException.class, CartIsEmptyException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<String> handleCartExceptions(RuntimeException ex, ServerWebExchange exchange) {
        exchange.getAttributes().put("errorMessage", ex.getMessage());
        return Mono.just("cart");
    }
}
