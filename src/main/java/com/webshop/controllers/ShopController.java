package com.webshop.controllers;

import com.webshop.entities.Product;
import com.webshop.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/web-shop")
public class ShopController {

    private final ProductService productService;

    @GetMapping("/products/{id}")
    public String getProduct(@PathVariable("id") Integer id, Model model) {
        Product product = productService.getProductById(id);
        model.addAttribute("product", product);
        return "product";
    }

    @GetMapping("/products")
    public String getProducts(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "4") int size,
        @RequestParam(required = false) String title,
        @RequestParam(required = false) Double minPrice,
        @RequestParam(required = false) Double maxPrice,
        @RequestParam(defaultValue = "asc") String sort,
        Model model) {

        Page<Product> products = productService.getProducts(title, minPrice, maxPrice, sort, page, size);

        model.addAttribute("products", products);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPages", products.getTotalPages());
        model.addAttribute("title", title);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("sort", sort);

        return "showcase";
    }


}
