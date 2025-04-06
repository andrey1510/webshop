package com.shopservice.controllers;

import com.shopservice.dto.ProductInputDto;
import com.shopservice.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
@RequestMapping("/product-creator")
public class ProductCreatorController {

    private final ProductService productService;

    @GetMapping
    public Mono<String> showForm(ServerWebExchange exchange) {
        return exchange.getSession()
            .flatMap(session -> {
                if (session.getAttributes().containsKey("successMessage")) {
                    exchange.getAttributes().put(
                        "successMessage",
                        session.getAttribute("successMessage")
                    );
                }
                return Mono.just("product-creator");
            });
    }

    @PostMapping
    public Mono<String> createProduct(
        @RequestPart("title") String title,
        @RequestPart("description") String description,
        @RequestPart("price") String priceStr,
        @RequestPart(value = "image", required = false) FilePart image,
        ServerWebExchange exchange) {

        ProductInputDto dto = ProductInputDto.fromFormData(
            title,
            description,
            priceStr,
            image
        );

        return productService.createProduct(dto)
            .doOnSuccess(p -> exchange.getSession()
                .doOnNext(session -> session.getAttributes().put("successMessage", "Товар успешно создан!"))
                .subscribe())
            .thenReturn("redirect:/product-creator");
    }

}
