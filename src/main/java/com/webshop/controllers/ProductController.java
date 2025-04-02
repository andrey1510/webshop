package com.webshop.controllers;

import com.webshop.dto.ProductPreviewDto;
import com.webshop.entities.OrderItem;
import com.webshop.entities.Product;
import com.webshop.exceptions.ProductNotFoundException;
import com.webshop.services.CartService;
import com.webshop.services.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final CartService cartService;

    @GetMapping("/{id}")
    public Mono<String> getProduct(
        @PathVariable("id") Integer productId,
        ServerWebExchange exchange) {

        Mono<Product> productMono = productService.getProductById(productId)
            .onErrorResume(ProductNotFoundException.class, e -> {
                exchange.getAttributes().put("errorMessage", e.getMessage());
                return Mono.empty();
            });

        Mono<Integer> cartQuantityMono = cartService.getCurrentCartNoProducts()
            .flatMap(cart -> cartService.findCartItemByProductId(cart, productId))
            .map(OrderItem::getQuantity)
            .defaultIfEmpty(0);

        return Mono.zip(productMono, cartQuantityMono)
            .flatMap(tuple -> {
                Product product = tuple.getT1();
                Integer quantity = tuple.getT2();

                exchange.getAttributes().put("product", product);
                exchange.getAttributes().put("cartProductQuantity", quantity);
                return Mono.just("product");
            })
            .switchIfEmpty(Mono.just("product"));
    }

    @GetMapping
    public Mono<String> getProducts(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "100") int size,
        @RequestParam(required = false) String title,
        @RequestParam(required = false) Double minPrice,
        @RequestParam(required = false) Double maxPrice,
        @RequestParam(defaultValue = "asc") String sort,
        ServerWebExchange exchange) {

        Mono<Page<ProductPreviewDto>> productsPageMono = productService.getPageableProductPreviewDtos(
                title, minPrice, maxPrice, sort, page, size)
            .doOnError(e -> log.error("Error loading products", e));

        Mono<Map<Integer, Integer>> cartQuantitiesMono = cartService.getCartProductsQuantity()
            .doOnNext(quantities -> log.info("Cart quantities: {}", quantities))
            .doOnError(e -> log.error("Error loading cart quantities", e))
            .defaultIfEmpty(Collections.emptyMap());

        return Mono.zip(productsPageMono, cartQuantitiesMono)
            .flatMap(tuple -> {
                Page<ProductPreviewDto> productsPage = tuple.getT1();
                Map<Integer, Integer> cartQuantities = tuple.getT2();

                Map<String, Object> model = new HashMap<>();
                model.put("products", productsPage.getContent());
                model.put("productsPage", productsPage);
                model.put("currentPage", page);
                model.put("pageSize", size);
                model.put("sort", sort);
                model.put("cartProductsQuantities", cartQuantities);

                if (title != null) model.put("title", title);
                if (minPrice != null) model.put("minPrice", minPrice);
                if (maxPrice != null) model.put("maxPrice", maxPrice);

                exchange.getAttributes().clear();
                model.forEach((key, value) -> {
                    if (value != null) {
                        exchange.getAttributes().put(key, value);
                    }
                });

                return Mono.just("products");
            });
    }

    @ExceptionHandler(ProductNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<String> handleProductNotFoundException(
        ProductNotFoundException ex,
        ServerWebExchange exchange) {
        exchange.getAttributes().put("errorMessage", ex.getMessage());
        exchange.getAttributes().put("product", null);
        return Mono.just("product");
    }
}
