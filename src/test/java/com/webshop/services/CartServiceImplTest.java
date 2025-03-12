package com.webshop.services;

import com.webshop.entities.CustomerOrder;
import com.webshop.entities.OrderItem;
import com.webshop.entities.OrderStatus;
import com.webshop.entities.Product;
import com.webshop.exceptions.AlreadyInCartException;
import com.webshop.exceptions.CartIsEmptyException;
import com.webshop.exceptions.ItemIsNotInCartException;
import com.webshop.exceptions.WrongQuantityException;
import com.webshop.repositories.CustomerOrderRepository;
import com.webshop.repositories.OrderItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CartServiceImplTest {

    @Mock
    private CustomerOrderRepository customerOrderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ProductService productService;

    @InjectMocks
    private CartServiceImpl cartService;

    private CustomerOrder orderInCart;
    private Product product;
    private OrderItem orderItem;

    @BeforeEach
    public void setUp() {

        orderInCart = CustomerOrder.builder()
            .status(OrderStatus.CART)
            .items(new ArrayList<>())
            .build();

        product = Product.builder()
            .id(1)
            .price(100.0)
            .build();

        orderItem = OrderItem.builder()
            .product(product)
            .quantity(2)
            .customerOrder(orderInCart)
            .build();

    }

    @Test
    public void testGetCurrentCart_ExistingCart() {

        when(customerOrderRepository.findByStatus(OrderStatus.CART))
            .thenReturn(Optional.of(orderInCart));

        CustomerOrder result = cartService.getCurrentCart();

        assertNotNull(result);
        assertEquals(OrderStatus.CART, result.getStatus());

        verify(customerOrderRepository, times(1)).findByStatus(OrderStatus.CART);
        verify(customerOrderRepository, never()).save(any());
    }

    @Test
    public void testGetCurrentCart_NewCart() {

        when(customerOrderRepository.findByStatus(OrderStatus.CART))
            .thenReturn(Optional.empty());
        when(customerOrderRepository.save(any(CustomerOrder.class))).thenReturn(orderInCart);

        CustomerOrder result = cartService.getCurrentCart();

        assertNotNull(result);
        assertEquals(OrderStatus.CART, result.getStatus());

        verify(customerOrderRepository, times(1)).findByStatus(OrderStatus.CART);
        verify(customerOrderRepository, times(1)).save(any(CustomerOrder.class));
    }

    @Test
    public void testGetCartProductsQuantity() {

        when(customerOrderRepository.findByStatus(OrderStatus.CART))
            .thenReturn(Optional.of(orderInCart));

        orderInCart.getItems().add(orderItem);

        Map<Integer, Integer> result = cartService.getCartProductsQuantity();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(2, result.get(1));

        verify(customerOrderRepository, times(1)).findByStatus(OrderStatus.CART);
    }

    @Test
    public void testCreateNewCart() {

        when(customerOrderRepository.save(any(CustomerOrder.class))).thenReturn(orderInCart);

        CustomerOrder result = cartService.createNewCart();

        assertNotNull(result);
        assertEquals(OrderStatus.CART, result.getStatus());
        assertTrue(result.getItems().isEmpty());

        verify(customerOrderRepository, times(1)).save(any(CustomerOrder.class));
    }

    @Test
    public void testCompleteOrder_Success() {

        when(customerOrderRepository.findByStatus(OrderStatus.CART))
            .thenReturn(Optional.of(orderInCart));
        when(customerOrderRepository.save(any(CustomerOrder.class))).thenReturn(orderInCart);

        orderInCart.getItems().add(orderItem);

        CustomerOrder result = cartService.completeOrder();

        assertNotNull(result);
        assertEquals(OrderStatus.COMPLETED, result.getStatus());
        assertEquals(200.0, result.getCompletedOrderPrice());

        verify(customerOrderRepository, times(1)).findByStatus(OrderStatus.CART);
        verify(customerOrderRepository, times(1)).save(orderInCart);
    }

    @Test
    public void testCompleteOrder_EmptyCart() {

        when(customerOrderRepository.findByStatus(OrderStatus.CART))
            .thenReturn(Optional.of(orderInCart));

        orderInCart.getItems().clear();

        assertThrows(CartIsEmptyException.class, () -> cartService.completeOrder());

        verify(customerOrderRepository, times(1)).findByStatus(OrderStatus.CART);
        verify(customerOrderRepository, never()).save(any());
    }

    @Test
    public void testAddItemToCart_Success() {

        when(customerOrderRepository.findByStatus(OrderStatus.CART))
            .thenReturn(Optional.of(orderInCart));
        when(productService.getProductById(1)).thenReturn(product);
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(orderItem);
        when(customerOrderRepository.save(any(CustomerOrder.class))).thenReturn(orderInCart);

        cartService.addItemToCart(1, 2);

        assertEquals(1, orderInCart.getItems().size());
        assertEquals(1, orderInCart.getItems().get(0).getProduct().getId());
        assertEquals(2, orderInCart.getItems().get(0).getQuantity());

        verify(customerOrderRepository, times(1)).findByStatus(OrderStatus.CART);
        verify(productService, times(1)).getProductById(1);
        verify(orderItemRepository, times(1)).save(any(OrderItem.class));
        verify(customerOrderRepository, times(1)).save(orderInCart);
    }

    @Test
    public void testAddItemToCart_AlreadyInCart() {

        when(customerOrderRepository.findByStatus(OrderStatus.CART))
            .thenReturn(Optional.of(orderInCart));

        orderInCart.getItems().add(orderItem);

        assertThrows(AlreadyInCartException.class, () -> cartService.addItemToCart(1, 2));

        verify(customerOrderRepository, times(1)).findByStatus(OrderStatus.CART);
        verify(productService, never()).getProductById(any());
        verify(orderItemRepository, never()).save(any());
        verify(customerOrderRepository, never()).save(any());
    }

    @Test
    public void testAddItemToCart_InvalidQuantity() {

        assertThrows(WrongQuantityException.class, () -> cartService.addItemToCart(1, 0));

        verify(customerOrderRepository, never()).findByStatus(any());
        verify(productService, never()).getProductById(any());
        verify(orderItemRepository, never()).save(any());
        verify(customerOrderRepository, never()).save(any());
    }

    @Test
    public void testUpdateItemQuantity_Success() {

        when(customerOrderRepository.findByStatus(OrderStatus.CART))
            .thenReturn(Optional.of(orderInCart));
        when(customerOrderRepository.save(any(CustomerOrder.class))).thenReturn(orderInCart);

        orderInCart.getItems().add(orderItem);

        cartService.updateItemQuantity(1, 3);

        assertEquals(3, orderInCart.getItems().get(0).getQuantity());

        verify(customerOrderRepository, times(1)).findByStatus(OrderStatus.CART);
        verify(customerOrderRepository, times(1)).save(orderInCart);
    }

    @Test
    public void testUpdateItemQuantity_ItemNotFound() {

        when(customerOrderRepository.findByStatus(OrderStatus.CART))
            .thenReturn(Optional.of(orderInCart));

        assertThrows(ItemIsNotInCartException.class, () -> cartService.updateItemQuantity(1, 3));

        verify(customerOrderRepository, times(1)).findByStatus(OrderStatus.CART);
        verify(customerOrderRepository, never()).save(any());
    }

    @Test
    public void testUpdateItemQuantity_InvalidQuantity() {

        assertThrows(WrongQuantityException.class, () -> cartService.updateItemQuantity(1, 0));

        verify(customerOrderRepository, never()).findByStatus(any());
        verify(customerOrderRepository, never()).save(any());
    }

    @Test
    public void testRemoveCartItem_Success() {

        when(customerOrderRepository.findByStatus(OrderStatus.CART))
            .thenReturn(Optional.of(orderInCart));
        when(customerOrderRepository.save(any(CustomerOrder.class))).thenReturn(orderInCart);

        orderInCart.getItems().add(orderItem);

        cartService.removeCartItem(1);

        assertTrue(orderInCart.getItems().isEmpty());

        verify(customerOrderRepository, times(1)).findByStatus(OrderStatus.CART);
        verify(orderItemRepository, times(1)).delete(orderItem);
        verify(customerOrderRepository, times(1)).save(orderInCart);
    }

    @Test
    public void testRemoveCartItem_ItemNotFound() {

        when(customerOrderRepository.findByStatus(OrderStatus.CART))
            .thenReturn(Optional.of(orderInCart));
        when(customerOrderRepository.save(any(CustomerOrder.class))).thenReturn(orderInCart);

        cartService.removeCartItem(1);

        assertTrue(orderInCart.getItems().isEmpty());

        verify(customerOrderRepository, times(1)).findByStatus(OrderStatus.CART);
        verify(orderItemRepository, never()).delete(any());
        verify(customerOrderRepository, times(1)).save(orderInCart);
    }

}