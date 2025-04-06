package com.shopservice.services;

import com.shopservice.dto.ProductInputDto;
import com.shopservice.dto.ProductPreviewDto;
import com.shopservice.entities.Product;
import org.springframework.data.domain.Page;
import reactor.core.publisher.Mono;

public interface ProductService {

    Mono<Product> createProduct(ProductInputDto productInputDto);

    Mono<Product> getProductById(Integer id);

    Mono<Page<ProductPreviewDto>> getPageableProductPreviewDtos(String title, Double minPrice, Double maxPrice, String sort, int page, int size);
}
