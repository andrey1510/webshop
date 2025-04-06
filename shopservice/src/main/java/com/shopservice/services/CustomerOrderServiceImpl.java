package com.shopservice.services;

import com.shopservice.entities.CustomerOrder;
import com.shopservice.entities.OrderStatus;
import com.shopservice.exceptions.CompletedCustomerOrderNotFoundException;
import com.shopservice.repositories.CustomerOrderRepository;
import com.shopservice.repositories.OrderItemProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@RequiredArgsConstructor
@Service
public class CustomerOrderServiceImpl implements CustomerOrderService {

    private final CustomerOrderRepository customerOrderRepository;
    private final OrderItemProductRepository orderItemProductRepository;

    @Override
    public Mono<CustomerOrder> getCompletedOrderById(Integer orderId) {
        return customerOrderRepository.findByIdAndStatus(orderId, OrderStatus.COMPLETED)
            .switchIfEmpty(Mono.error(new CompletedCustomerOrderNotFoundException("Заказ не найден")))
            .flatMap(order ->
                orderItemProductRepository.findByCustomerOrderIdWithProduct(orderId)
                    .collectList()
                    .doOnNext(order::setItems)
                    .thenReturn(order)
            );
    }

    @Override
    public Flux<CustomerOrder> getCompletedOrders() {
        return customerOrderRepository.findAllByStatus(OrderStatus.COMPLETED);
    }

    @Override
    public Mono<Double> getTotalPriceOfCompletedOrders() {
        return customerOrderRepository.findAllByStatus(OrderStatus.COMPLETED)
            .map(CustomerOrder::getCompletedOrderPrice)
            .reduce(0.0, Double::sum);
    }

}
