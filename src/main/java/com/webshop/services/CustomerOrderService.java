package com.webshop.services;

import com.webshop.entities.CustomerOrder;

import java.util.List;

public interface CustomerOrderService {

    CustomerOrder getOrderById(Integer orderId);

    List<CustomerOrder> getCompletedOrders();

    Double getTotalPriceOfCompletedOrders();
}
