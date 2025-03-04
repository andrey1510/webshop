package com.webshop.services;

import com.webshop.entities.CustomerOrder;
import com.webshop.entities.OrderStatus;
import com.webshop.exceptions.CustomerOrderNotFoundException;
import com.webshop.repositories.CustomerOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CustomerOrderServiceImpl implements CustomerOrderService {

    private final CustomerOrderRepository customerOrderRepository;

    @Transactional
    @Override
    public CustomerOrder getOrderById(Integer orderId) {
        return customerOrderRepository.findById(orderId)
            .orElseThrow(() -> new CustomerOrderNotFoundException("Заказ не найден"));
    }

    @Transactional
    @Override
    public List<CustomerOrder> getCompletedOrders() {
        return customerOrderRepository.findAllByStatus(OrderStatus.COMPLETED);
    }

    @Override
    @Transactional
    public Double getTotalPriceOfCompletedOrders() {
        return getCompletedOrders().stream()
            .mapToDouble(CustomerOrder::getCompletedOrderPrice)
            .sum();
    }

}
