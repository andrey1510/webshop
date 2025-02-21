package com.webshop.services;

import com.webshop.dto.ProductInputDto;
import com.webshop.entities.Product;
import org.springframework.data.domain.Page;

public interface ProductService {

    Product createProduct(ProductInputDto productInputDto);

    Product getProductById(Integer id);

    Page<ProductInputDto> getAllProductsPaginatedAsDto(int page, int size);

    Page<ProductInputDto> findProductsByTitlePaginatedAsDto(String searchQuery, int page, int size);
}
