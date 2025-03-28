package com.webshop.controllers;

import com.webshop.exceptions.CompletedCustomerOrderNotFoundException;
import com.webshop.services.CustomerOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final CustomerOrderService customerOrderService;

    @GetMapping("/{id}")
    public Mono<Rendering> getCompletedOrder(@PathVariable("id") Integer orderId) {
        return customerOrderService.getCompletedOrderById(orderId)
            .map(completedOrder -> Rendering.view("order")
                .modelAttribute("order", completedOrder)
                .build())
            .switchIfEmpty(Mono.error(new CompletedCustomerOrderNotFoundException("Заказ не найден")));
    }

    @GetMapping
    public Mono<Rendering> getAllCompletedOrders() {
        return customerOrderService.getCompletedOrders()
            .collectList()
            .flatMap(completedOrders -> customerOrderService.getTotalPriceOfCompletedOrders()
                .map(totalPrice -> Rendering.view("orders")
                    .modelAttribute("orders", completedOrders)
                    .modelAttribute("totalPrice", totalPrice)
                    .build()));
    }

    @ExceptionHandler(CompletedCustomerOrderNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<Rendering> handleCustomerOrderNotFoundException(CompletedCustomerOrderNotFoundException ex) {
        return Mono.just(Rendering.view("order")
            .modelAttribute("errorMessage", ex.getMessage())
            .modelAttribute("order", null)
            .build());
    }
}
