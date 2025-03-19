package com.webshop.repositories;

import com.webshop.entities.Product;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends R2dbcRepository<Product, Integer> {
}
