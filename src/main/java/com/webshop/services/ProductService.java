package com.webshop.services;

import com.webshop.dto.ProductInputDto;
import com.webshop.dto.ProductPreviewDto;
import com.webshop.entities.Product;
import org.springframework.data.domain.Page;
import reactor.core.publisher.Mono;

public interface ProductService {

    Mono<Product> createProduct(ProductInputDto productInputDto);

    Mono<Product> getProductById(Integer id);

    Mono<Page<ProductPreviewDto>> getPageableProductPreviewDtos(String title, Double minPrice, Double maxPrice, String sort, int page, int size);
}
