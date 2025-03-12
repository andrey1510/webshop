package com.webshop.controllers;

import com.webshop.dto.ProductPreviewDto;
import com.webshop.entities.CustomerOrder;
import com.webshop.entities.OrderItem;
import com.webshop.entities.Product;
import com.webshop.exceptions.ProductNotFoundException;
import com.webshop.services.CartService;
import com.webshop.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final CartService cartService;

    @GetMapping("/{id}")
    public String getProduct(@PathVariable("id") Integer productId, Model model) {
        Product product = productService.getProductById(productId);
        model.addAttribute("product", product);

        CustomerOrder cart = cartService.getCurrentCart();
        OrderItem cartItem = cartService.findCartItemByProductId(cart, productId);
        model.addAttribute("cartProductQuantity", cartItem != null ? cartItem.getQuantity() : 0);

        return "product";
    }

    @GetMapping
    public String getProducts(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "100") int size,
        @RequestParam(required = false) String title,
        @RequestParam(required = false) Double minPrice,
        @RequestParam(required = false) Double maxPrice,
        @RequestParam(defaultValue = "asc") String sort,
        Model model) {

        Page<ProductPreviewDto> products = productService.getProductPreviewDtos(title, minPrice, maxPrice, sort, page, size);

        Map<Integer, Integer> cartProductsQuantities = cartService.getCartProductsQuantity();

        model.addAttribute("products", products);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPages", products.getTotalPages());
        model.addAttribute("title", title);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("sort", sort);
        model.addAttribute("cartProductsQuantities", cartProductsQuantities);

        return "products";
    }


    @ExceptionHandler(ProductNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleProductNotFoundException(ProductNotFoundException ex) {
        ModelAndView modelAndView = new ModelAndView("product");
        modelAndView.addObject("errorMessage", ex.getMessage());
        modelAndView.addObject("product", null);
        return modelAndView;
    }
}
