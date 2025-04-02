package com.webshop.repositories;

import com.webshop.entities.Product;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;


@Repository
public interface ProductRepository extends R2dbcRepository<Product, Integer> {
    @Query("SELECT COUNT(*) FROM products WHERE title LIKE CONCAT('%', :title, '%')")
    Mono<Long> countByTitleContaining(String title);

    @Query("SELECT COUNT(*) FROM products WHERE price BETWEEN :minPrice AND :maxPrice")
    Mono<Long> countByPriceBetween(Double minPrice, Double maxPrice);

    @Query("SELECT COUNT(*) FROM products WHERE price > :price")
    Mono<Long> countByPriceGreaterThan(Double price);

    @Query("SELECT COUNT(*) FROM products WHERE price < :price")
    Mono<Long> countByPriceLessThan(Double price);
}
