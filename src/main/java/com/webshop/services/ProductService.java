package com.webshop.services;

import com.webshop.dto.ProductInputDto;
import com.webshop.dto.ProductPreviewDto;
import com.webshop.entities.Product;
import org.springframework.data.domain.Page;

public interface ProductService {

    Product createProduct(ProductInputDto productInputDto);

    Product getProductById(Integer id);

    Page<ProductPreviewDto> getProductPreviewDtos(String title, Double minPrice, Double maxPrice, String sort, int page, int size);
}
