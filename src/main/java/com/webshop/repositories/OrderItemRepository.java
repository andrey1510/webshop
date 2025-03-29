package com.webshop.repositories;

import com.webshop.entities.OrderItem;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface OrderItemRepository extends R2dbcRepository<OrderItem, Integer> {
    Flux<OrderItem> findByCustomerOrderId(Integer customerOrderId);

}
