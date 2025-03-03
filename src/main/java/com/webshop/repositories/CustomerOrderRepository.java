package com.webshop.repositories;

import com.webshop.entities.CustomerOrder;
import com.webshop.entities.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Integer> {

    Optional<CustomerOrder> findByStatus(OrderStatus status);

    List<CustomerOrder> findAllByStatus(OrderStatus status);
}
