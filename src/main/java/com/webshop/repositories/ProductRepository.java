package com.webshop.repositories;

import com.webshop.entities.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    Page<Product> findByTitleContaining(String title, Pageable pageable);

    Page<Product> findByPriceGreaterThan(Double price, Pageable pageable);

    Page<Product> findByPriceLessThan(Double price, Pageable pageable);

    Page<Product> findByPriceBetween(Double minPrice, Double maxPrice, Pageable pageable);
}
