package com.webshop.controllers;

import com.webshop.dto.ProductInputDto;
import com.webshop.entities.Product;
import com.webshop.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequiredArgsConstructor
@RequestMapping("/product-creator")
public class ProductCreatorController {

    private final ProductService productService;

    @PostMapping
    public String createPost(@ModelAttribute ProductInputDto productInputDto,
                             @RequestParam(value = "image", required = false) MultipartFile image) {
        productInputDto.setImage(image);
        productService.createProduct(productInputDto);
        return "redirect:/product-creator";
    }

}
