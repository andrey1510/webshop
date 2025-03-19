package com.webshop.services;

import com.webshop.entities.CustomerOrder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface CustomerOrderService {

    Mono<CustomerOrder> getCompletedOrderById(Integer orderId);

    Flux<CustomerOrder> getCompletedOrders();

    Mono<Double> getTotalPriceOfCompletedOrders();
}
