package com.webshop.repositories;

import com.webshop.entities.CustomerOrder;
import com.webshop.entities.OrderStatus;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface CustomerOrderRepository extends R2dbcRepository<CustomerOrder, Integer> {

    Mono<CustomerOrder> findByStatus(OrderStatus status);

    @Query("SELECT * FROM customer_orders WHERE id = :id AND status = :status")
    Mono<CustomerOrder> findByIdAndStatus(Integer id, OrderStatus status);

    @Query("SELECT * FROM customer_orders WHERE status = :status")
    Flux<CustomerOrder> findAllByStatus(OrderStatus status);


}
