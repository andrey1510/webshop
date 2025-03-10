package com.webshop.repositories;

import com.webshop.dto.ProductPreviewDto;
import com.webshop.entities.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    @Query("""
           SELECT new com.webshop.dto.ProductPreviewDto(p.id, p.title, p.price, p.imagePath)
           FROM Product p
           WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :title, '%'))
           """)
    Page<ProductPreviewDto> findProductPreviewDtosByTitleContaining(@Param("title") String title, Pageable pageable);

    @Query("""
            SELECT new com.webshop.dto.ProductPreviewDto(p.id, p.title, p.price, p.imagePath)
            FROM Product p
            WHERE p.price > :price
            """)
    Page<ProductPreviewDto> findProductPreviewDtosByPriceGreaterThan(Double price, Pageable pageable);

    @Query("""
            SELECT new com.webshop.dto.ProductPreviewDto(p.id, p.title, p.price, p.imagePath)
            FROM Product p
            WHERE p.price < :price
            """)
    Page<ProductPreviewDto> findProductPreviewDtosByPriceLessThan(Double price, Pageable pageable);

    @Query("""
            SELECT new com.webshop.dto.ProductPreviewDto(p.id, p.title, p.price, p.imagePath)
            FROM Product p
            WHERE p.price BETWEEN :minPrice AND :maxPrice
            """)
    Page<ProductPreviewDto> findProductPreviewDtosByPriceBetween(Double minPrice, Double maxPrice, Pageable pageable);

    @Query("""
            SELECT new com.webshop.dto.ProductPreviewDto(p.id, p.title, p.price, p.imagePath)
            FROM Product p
            """)
    Page<ProductPreviewDto> findAllProductPreviewDtos(Pageable pageable);
}
