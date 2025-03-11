package com.webshop.repositories;

import com.webshop.dto.ProductPreviewDto;
import com.webshop.entities.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Sql("/test-data-products.sql")
@ActiveProfiles("test")
public class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    public void testFindAllProductPreviewDtos() {
        Page<ProductPreviewDto> result = productRepository
            .findAllProductPreviewDtos(PageRequest.of(0, 10));

        assertEquals(3, result.getContent().size());
        assertEquals("Ноутбук", result.getContent().get(0).title());
        assertEquals("Смартфон", result.getContent().get(1).title());
        assertEquals("Планшет", result.getContent().get(2).title());

    }

    @Test
    public void testFindProductPreviewDtosByTitleContaining() {
        Page<ProductPreviewDto> result = productRepository
            .findProductPreviewDtosByTitleContaining("ноут", PageRequest.of(0, 10));

        assertEquals(1, result.getContent().size());
        assertEquals("Ноутбук", result.getContent().get(0).title());
    }

    @Test
    public void testFindProductPreviewDtosByPriceGreaterThan() {
        Page<ProductPreviewDto> result = productRepository
            .findProductPreviewDtosByPriceGreaterThan(400.0, PageRequest.of(0, 10));

        assertEquals(2, result.getContent().size());
        assertEquals("Ноутбук", result.getContent().get(0).title());
        assertEquals("Смартфон", result.getContent().get(1).title());
    }

    @Test
    public void testFindProductPreviewDtosByPriceLessThan() {
        Page<ProductPreviewDto> result = productRepository
            .findProductPreviewDtosByPriceLessThan(400.0, PageRequest.of(0, 10));

        assertEquals(1, result.getContent().size());
        assertEquals("Планшет", result.getContent().get(0).title());
    }

    @Test
    public void testFindProductPreviewDtosByPriceBetween() {
        Page<ProductPreviewDto> result = productRepository
            .findProductPreviewDtosByPriceBetween(400.0, 1000.0, PageRequest.of(0, 10));

        assertEquals(2, result.getContent().size());
        assertEquals("Ноутбук", result.getContent().get(0).title());
        assertEquals("Смартфон", result.getContent().get(1).title());
    }

    @Test
    public void testFindById() {
        Optional<Product> foundProduct = productRepository.findById(6);

        assertTrue(foundProduct.isPresent());

        Product product = foundProduct.get();
        assertEquals("Ноутбук", product.getTitle());
    }

}
