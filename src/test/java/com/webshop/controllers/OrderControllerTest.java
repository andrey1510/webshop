package com.webshop.controllers;

import com.webshop.entities.CustomerOrder;
import com.webshop.entities.OrderStatus;
import com.webshop.exceptions.CompletedCustomerOrderNotFoundException;
import com.webshop.services.CustomerOrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private CustomerOrderService customerOrderService;

    @Mock
    private Model model;

    @InjectMocks
    private OrderController orderController;

    private Integer orderId;
    private CustomerOrder completedOrder;
    private List<CustomerOrder> completedOrders;
    private Double totalPrice;

    @BeforeEach
    void setUp() {

        orderId = 1;
        completedOrder = CustomerOrder.builder()
            .id(orderId)
            .status(OrderStatus.COMPLETED)
            .build();

        completedOrders = Collections.singletonList(completedOrder);

        totalPrice = 100.0;
    }

    @Test
    void testGetCompletedOrder() {

        when(customerOrderService.getCompletedOrderById(orderId)).thenReturn(completedOrder);

        String viewName = orderController.getCompletedOrder(orderId, model);

        assertEquals("order", viewName);
        verify(customerOrderService, times(1)).getCompletedOrderById(orderId);
        verify(model, times(1)).addAttribute("order", completedOrder);
    }

    @Test
    void testGetAllCompletedOrders() {

        when(customerOrderService.getCompletedOrders()).thenReturn(completedOrders);
        when(customerOrderService.getTotalPriceOfCompletedOrders()).thenReturn(totalPrice);

        String viewName = orderController.getAllCompletedOrders(model);

        assertEquals("orders", viewName);
        verify(customerOrderService, times(1)).getCompletedOrders();
        verify(customerOrderService, times(1)).getTotalPriceOfCompletedOrders();
        verify(model, times(1)).addAttribute("orders", completedOrders);
        verify(model, times(1)).addAttribute("totalPrice", totalPrice);
    }

    @Test
    void testHandleCustomerOrderNotFoundException() {

        ModelAndView modelAndView = orderController
            .handleCustomerOrderNotFoundException(new CompletedCustomerOrderNotFoundException("Заказ не найден"));

        assertEquals("order", modelAndView.getViewName());
        assertEquals("Заказ не найден", modelAndView.getModel().get("errorMessage"));
        assertNull(modelAndView.getModel().get("order"));
    }
}
