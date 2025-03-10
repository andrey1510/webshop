package com.webshop.controllers;

import com.webshop.entities.CustomerOrder;
import com.webshop.exceptions.CompletedCustomerOrderNotFoundException;
import com.webshop.services.CustomerOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final CustomerOrderService customerOrderService;

    @GetMapping("/{id}")
    public String getCompletedOrder(@PathVariable("id") Integer orderId, Model model) {
        CustomerOrder completedOrder = customerOrderService.getCompletedOrderById(orderId);
        model.addAttribute("order", completedOrder);
        return "order";
    }

    @GetMapping
    public String getAllCompletedOrders(Model model) {

        List<CustomerOrder> completedOrders = customerOrderService.getCompletedOrders();

        Double totalPrice = customerOrderService.getTotalPriceOfCompletedOrders();

        model.addAttribute("orders", completedOrders);
        model.addAttribute("totalPrice", totalPrice);

        return "orders";
    }

    @ExceptionHandler(CompletedCustomerOrderNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleCustomerOrderNotFoundException(CompletedCustomerOrderNotFoundException ex) {
        ModelAndView modelAndView = new ModelAndView("order");
        modelAndView.addObject("errorMessage", ex.getMessage());
        modelAndView.addObject("order", null);
        return modelAndView;
    }

}
