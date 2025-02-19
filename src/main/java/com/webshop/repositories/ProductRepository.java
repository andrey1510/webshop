package com.webshop.repositories;

import com.webshop.dto.ProductDto;
import com.webshop.entities.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    @Query("SELECT new com.webshop.dto.ProductDto(p.id, p.title, p.price, p.imageUrl) " +
            "FROM Product p")
    Page<ProductDto> findAllAsDto(Pageable pageable);

    @Query("SELECT new com.webshop.dto.ProductDto(p.id, p.title, p.price, p.imageUrl) " +
            "FROM Product p " +
            "WHERE LOWER(p.title) LIKE CONCAT('%', LOWER(:searchQuery), '%')")
    Page<ProductDto> findProductsByTitleAsDto(@Param("searchQuery") String searchQuery, Pageable pageable);
}
