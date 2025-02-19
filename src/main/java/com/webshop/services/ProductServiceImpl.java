package com.webshop.services;

import com.webshop.dto.ProductDto;
import com.webshop.entities.Product;
import com.webshop.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;



@RequiredArgsConstructor
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public Product findProductById(Integer id) {
        return productRepository.findById(id).orElse(null);
    }

    @Override
    public Page<ProductDto> getAllProductsPaginatedAsDto(int page, int size) {
        return productRepository.findAllAsDto(PageRequest.of(page, size));
    }

    @Override
    public Page<ProductDto> findProductsByTitlePaginatedAsDto(String searchQuery, int page, int size) {
        return productRepository.findProductsByTitleAsDto(searchQuery, PageRequest.of(page, size));
    }
}
