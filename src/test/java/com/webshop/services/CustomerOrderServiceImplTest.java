package com.webshop.services;

import com.webshop.entities.CustomerOrder;
import com.webshop.entities.OrderStatus;
import com.webshop.exceptions.CompletedCustomerOrderNotFoundException;
import com.webshop.repositories.CustomerOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomerOrderServiceImplTest {

    @Mock
    private CustomerOrderRepository customerOrderRepository;

    @InjectMocks
    private CustomerOrderServiceImpl customerOrderService;

    private CustomerOrder completedOrder;
    private CustomerOrder orderInCart;

    @BeforeEach
    public void setUp() {

        completedOrder = CustomerOrder.builder()
            .id(1)
            .status(OrderStatus.COMPLETED)
            .completedOrderPrice(100.0)
            .build();

        orderInCart = CustomerOrder.builder()
            .id(2)
            .status(OrderStatus.CART)
            .completedOrderPrice(0.0)
            .build();

    }

    @Test
    public void testGetCompletedOrderById_Success() {

        when(customerOrderRepository.findById(1)).thenReturn(Optional.of(completedOrder));

        CustomerOrder result = customerOrderService.getCompletedOrderById(1);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(OrderStatus.COMPLETED, result.getStatus());
        assertEquals(100.0, result.getCompletedOrderPrice());

        verify(customerOrderRepository, times(1)).findById(1);
    }

    @Test
    public void testGetCompletedOrderById_NotFound() {

        when(customerOrderRepository.findById(3)).thenReturn(Optional.empty());

        assertThrows(CompletedCustomerOrderNotFoundException.class, () -> {
            customerOrderService.getCompletedOrderById(3);
        });

        verify(customerOrderRepository, times(1)).findById(3);
    }

    @Test
    public void testGetCompletedOrderById_NotCompleted() {

        when(customerOrderRepository.findById(2)).thenReturn(Optional.of(orderInCart));

        assertThrows(CompletedCustomerOrderNotFoundException.class, () -> {
            customerOrderService.getCompletedOrderById(2);
        });

        verify(customerOrderRepository, times(1)).findById(2);
    }

    @Test
    public void testGetCompletedOrders_Success() {

        when(customerOrderRepository.findAllByStatus(OrderStatus.COMPLETED))
            .thenReturn(Arrays.asList(completedOrder));

        List<CustomerOrder> result = customerOrderService.getCompletedOrders();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getId());
        assertEquals(OrderStatus.COMPLETED, result.get(0).getStatus());

        verify(customerOrderRepository, times(1)).findAllByStatus(OrderStatus.COMPLETED);
    }

    @Test
    public void testGetCompletedOrders_EmptyList() {

        when(customerOrderRepository.findAllByStatus(OrderStatus.COMPLETED))
            .thenReturn(Collections.emptyList());

        List<CustomerOrder> result = customerOrderService.getCompletedOrders();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(customerOrderRepository, times(1)).findAllByStatus(OrderStatus.COMPLETED);
    }

    @Test
    public void testGetTotalPriceOfCompletedOrders_Success() {

        when(customerOrderRepository.findAllByStatus(OrderStatus.COMPLETED))
            .thenReturn(Arrays.asList(completedOrder));

        Double result = customerOrderService.getTotalPriceOfCompletedOrders();

        assertNotNull(result);
        assertEquals(100.0, result);

        verify(customerOrderRepository, times(1)).findAllByStatus(OrderStatus.COMPLETED);
    }

    @Test
    public void testGetTotalPriceOfCompletedOrders_EmptyList() {

        when(customerOrderRepository.findAllByStatus(OrderStatus.COMPLETED))
            .thenReturn(Collections.emptyList());

        Double result = customerOrderService.getTotalPriceOfCompletedOrders();

        assertNotNull(result);
        assertEquals(0.0, result);

        verify(customerOrderRepository, times(1)).findAllByStatus(OrderStatus.COMPLETED);
    }

}
