package com.webshop.services;

import com.webshop.entities.CustomerOrder;
import com.webshop.entities.OrderStatus;
import com.webshop.exceptions.CompletedCustomerOrderNotFoundException;
import com.webshop.repositories.CustomerOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CustomerOrderServiceImpl implements CustomerOrderService {

    private final CustomerOrderRepository customerOrderRepository;

    @Transactional
    @Override
    public Mono<CustomerOrder> getCompletedOrderById(Integer orderId) {
        return customerOrderRepository.findByIdAndStatus(orderId, OrderStatus.COMPLETED)
            .switchIfEmpty(Mono.error(new CompletedCustomerOrderNotFoundException("Заказ не найден")));
    }

    @Transactional
    @Override
    public Flux<CustomerOrder> getCompletedOrders() {
        return customerOrderRepository.findAllByStatus(OrderStatus.COMPLETED);
    }

    @Override
    @Transactional
    public Mono<Double> getTotalPriceOfCompletedOrders() {
        return customerOrderRepository.findAllByStatus(OrderStatus.COMPLETED)
            .map(CustomerOrder::getCompletedOrderPrice)
            .reduce(0.0, Double::sum);
    }

}
