package com.webshop.controllers;

import com.webshop.entities.CustomerOrder;
import com.webshop.entities.OrderStatus;
import com.webshop.exceptions.CartIsEmptyException;
import com.webshop.services.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    @Mock
    private CartService cartService;

    @Mock
    private Model model;

    @Mock
    private HttpServletRequest request;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private CartController cartController;

    private CustomerOrder orderInCart;
    private Double totalPrice;
    private Integer productId;
    private Integer quantity;

    @BeforeEach
    void setUp() {

        orderInCart = CustomerOrder.builder()
            .id(1)
            .status(OrderStatus.CART)
            .build();

        totalPrice = 100.0;
        productId = 1;
        quantity = 2;
    }

    @Test
    void testGetCart() {

        when(cartService.getCurrentCart()).thenReturn(orderInCart);
        when(cartService.calculateTotalPrice(orderInCart)).thenReturn(totalPrice);

        String viewName = cartController.getCart(model);

        assertEquals("cart", viewName);

        verify(cartService, times(1)).getCurrentCart();
        verify(cartService, times(1)).calculateTotalPrice(orderInCart);
        verify(model, times(1)).addAttribute("cart", orderInCart);
        verify(model, times(1)).addAttribute("totalPrice", totalPrice);
    }

    @Test
    void testAddCartItem() {

        when(request.getHeader("Referer")).thenReturn("/previous-page");

        String viewName = cartController.addCartItem(productId, quantity, request);

        assertEquals("redirect:/previous-page", viewName);

        verify(cartService, times(1)).addItemToCart(productId, quantity);
    }

    @Test
    void testUpdateCartItem() {

        when(request.getHeader("Referer")).thenReturn("/previous-page");

        String viewName = cartController.updateCartItem(productId, quantity, request);

        assertEquals("redirect:/previous-page", viewName);

        verify(cartService, times(1)).updateItemQuantity(productId, quantity);
    }

    @Test
    void testRemoveCartItem() {

        when(request.getHeader("Referer")).thenReturn("/previous-page");

        String viewName = cartController.removeCartItem(productId, request);

        assertEquals("redirect:/previous-page", viewName);

        verify(cartService, times(1)).removeCartItem(productId);
    }

    @Test
    void testCompleteOrder() {

        when(cartService.completeOrder()).thenReturn(orderInCart);

        String viewName = cartController.completeOrder();

        assertEquals("redirect:/orders/1", viewName);

        verify(cartService, times(1)).completeOrder();
    }

    @Test
    void testHandleCartIsEmptyException() {

        assertEquals("redirect:/cart",
            cartController.handleCartIsEmptyException(new CartIsEmptyException("Корзина пустая."), redirectAttributes));

        verify(redirectAttributes, times(1))
            .addFlashAttribute("errorMessage", "Корзина пустая.");
    }
}
