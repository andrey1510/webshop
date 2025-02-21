package com.webshop.repositories;

import com.webshop.dto.ProductInputDto;
import com.webshop.entities.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    //ToDo
    @Query("SELECT new com.webshop.dto.ProductInputDto(p.id, p.title, p.price, p.image) " +
            "FROM Product p")
    Page<ProductInputDto> findAllAsDto(Pageable pageable);

    //ToDo
    @Query("SELECT new com.webshop.dto.ProductInputDto(p.id, p.title, p.price, p.image) " +
            "FROM Product p " +
            "WHERE LOWER(p.title) LIKE CONCAT('%', LOWER(:searchQuery), '%')")
    Page<ProductInputDto> findProductsByTitleAsDto(@Param("searchQuery") String searchQuery, Pageable pageable);
}
