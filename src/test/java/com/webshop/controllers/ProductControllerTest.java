package com.webshop.controllers;

import com.webshop.dto.ProductPreviewDto;
import com.webshop.entities.CustomerOrder;
import com.webshop.entities.OrderItem;
import com.webshop.entities.Product;
import com.webshop.exceptions.ProductNotFoundException;
import com.webshop.services.CartService;
import com.webshop.services.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.ui.Model;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @Mock
    private CartService cartService;

    @Mock
    private Model model;

    @InjectMocks
    private ProductController productController;

    private Integer productId;
    private Product product;
    private CustomerOrder orderInCart;
    private OrderItem item;
    private Page<ProductPreviewDto> products;
    private Map<Integer, Integer> cartProductsQuantities;

    @BeforeEach
    void setUp() {

        product = Product.builder()
            .id(1)
            .title("Ноутбук")
            .build();

        orderInCart = new CustomerOrder();
        item = new OrderItem();
        item.setQuantity(2);

        ProductPreviewDto productPreviewDto = new ProductPreviewDto(
            1, "Ноутбук", 50.0, "laptop.jpg");
        products = new PageImpl<>(Collections.singletonList(productPreviewDto));

        cartProductsQuantities = new HashMap<>();
        cartProductsQuantities.put(1, 2);
    }

    @Test
    void testGetProduct() {

        when(productService.getProductById(productId)).thenReturn(product);
        when(cartService.getCurrentCart()).thenReturn(orderInCart);
        when(cartService.findCartItemByProductId(orderInCart, productId)).thenReturn(item);

        String viewName = productController.getProduct(productId, model);

        assertEquals("product", viewName);
        verify(productService, times(1)).getProductById(productId);
        verify(cartService, times(1)).getCurrentCart();
        verify(cartService, times(1)).findCartItemByProductId(orderInCart, productId);
        verify(model, times(1)).addAttribute("product", product);
        verify(model, times(1)).addAttribute("cartProductQuantity", 2);
    }

    @Test
    void testGetProducts() {

        int page = 0;
        int size = 4;
        String title = "Test";
        Double minPrice = 10.0;
        Double maxPrice = 100.0;
        String sort = "asc";

        when(productService.getProductPreviewDtos(title, minPrice, maxPrice, sort, page, size)).thenReturn(products);
        when(cartService.getCartProductsQuantity()).thenReturn(cartProductsQuantities);

        String viewName = productController.getProducts(page, size, title, minPrice, maxPrice, sort, model);

        assertEquals("products", viewName);
        verify(productService, times(1))
            .getProductPreviewDtos(title, minPrice, maxPrice, sort, page, size);
        verify(cartService, times(1)).getCartProductsQuantity();
        verify(model, times(1)).addAttribute("products", products);
        verify(model, times(1)).addAttribute("currentPage", page);
        verify(model, times(1)).addAttribute("pageSize", size);
        verify(model, times(1)).addAttribute("totalPages", products.getTotalPages());
        verify(model, times(1)).addAttribute("title", title);
        verify(model, times(1)).addAttribute("minPrice", minPrice);
        verify(model, times(1)).addAttribute("maxPrice", maxPrice);
        verify(model, times(1)).addAttribute("sort", sort);
        verify(model, times(1))
            .addAttribute("cartProductsQuantities", cartProductsQuantities);
    }

    @Test
    void testHandleProductNotFoundException() {

        ModelAndView modelAndView = productController
            .handleProductNotFoundException(new ProductNotFoundException("Товар не найден."));

        assertEquals("product", modelAndView.getViewName());
        assertEquals("Товар не найден.", modelAndView.getModel().get("errorMessage"));
        assertNull(modelAndView.getModel().get("product"));
    }
}
