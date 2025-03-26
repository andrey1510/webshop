package com.webshop.controllers;

import com.webshop.dto.ProductPreviewDto;
import com.webshop.entities.CustomerOrder;
import com.webshop.entities.OrderItem;
import com.webshop.entities.Product;
import com.webshop.exceptions.ProductNotFoundException;
import com.webshop.services.CartService;
import com.webshop.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final CartService cartService;
//
//    @GetMapping("/{id}")
//    public Mono<Rendering> getProduct(@PathVariable("id") Integer productId) {
//        return productService.getProductById(productId)
//            .flatMap(product -> cartService.getCurrentCart()
//                .map(cart -> {
//                    OrderItem cartItem = cartService.findCartItemByProductId(cart, productId);
//                    return Rendering.view("product")
//                        .modelAttribute("product", product)
//                        .modelAttribute("cartProductQuantity", cartItem != null ? cartItem.getQuantity() : 0)
//                        .build();
//                }))
//            .switchIfEmpty(Mono.error(new ProductNotFoundException("Товар не найден")));
//    }
//
//    @GetMapping
//    public Mono<Rendering> getProducts(
//        @RequestParam(defaultValue = "0") int page,
//        @RequestParam(defaultValue = "100") int size,
//        @RequestParam(required = false) String title,
//        @RequestParam(required = false) Double minPrice,
//        @RequestParam(required = false) Double maxPrice,
//        @RequestParam(defaultValue = "asc") String sort) {
//
//        return productService.getProductPreviewDtos(title, minPrice, maxPrice, sort, page, size)
//            .flatMap(products -> cartService.getCartProductsQuantity()
//                .map(cartProductsQuantities -> Rendering.view("products")
//                    .modelAttribute("products", products)
//                    .modelAttribute("currentPage", page)
//                    .modelAttribute("pageSize", size)
//                    .modelAttribute("totalPages", products.getTotalPages())
//                    .modelAttribute("title", title)
//                    .modelAttribute("minPrice", minPrice)
//                    .modelAttribute("maxPrice", maxPrice)
//                    .modelAttribute("sort", sort)
//                    .modelAttribute("cartProductsQuantities", cartProductsQuantities)
//                    .build()));
//    }
//
//    @ExceptionHandler(ProductNotFoundException.class)
//    @ResponseStatus(HttpStatus.NOT_FOUND)
//    public Mono<Rendering> handleProductNotFoundException(ProductNotFoundException ex) {
//        return Mono.just(Rendering.view("product")
//            .modelAttribute("errorMessage", ex.getMessage())
//            .modelAttribute("product", null)
//            .build());
//    }
}
