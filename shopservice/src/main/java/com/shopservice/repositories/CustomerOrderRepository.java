package com.shopservice.repositories;

import com.shopservice.entities.CustomerOrder;
import com.shopservice.entities.OrderStatus;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface CustomerOrderRepository extends R2dbcRepository<CustomerOrder, Integer> {

    Mono<CustomerOrder> findByStatusAndUserId(OrderStatus status, Integer userId);

    @Query("SELECT * FROM customer_orders WHERE id = :id AND status = :status AND user_id = :userId")
    Mono<CustomerOrder> findByIdAndStatusAndUserId(Integer id, OrderStatus status, Integer userId);

    @Query("SELECT * FROM customer_orders WHERE status = :status AND user_id = :userId")
    Flux<CustomerOrder> findAllByStatusAndUserId(OrderStatus status, Integer userId);

    Mono<CustomerOrder> findByStatus(OrderStatus status);

    @Query("SELECT * FROM customer_orders WHERE id = :id AND status = :status")
    Mono<CustomerOrder> findByIdAndStatus(Integer id, OrderStatus status);

    @Query("SELECT * FROM customer_orders WHERE status = :status")
    Flux<CustomerOrder> findAllByStatus(OrderStatus status);

}
