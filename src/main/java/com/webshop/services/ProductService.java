package com.webshop.services;

import com.webshop.dto.ProductDto;
import com.webshop.entities.Product;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductService {

    Product findProductById(Integer id);

    Page<ProductDto> getAllProductsPaginatedAsDto(int page, int size);

    Page<ProductDto> findProductsByTitlePaginatedAsDto(String searchQuery, int page, int size);
}
