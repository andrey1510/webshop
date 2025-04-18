package com.shopservice.repositories;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import reactor.test.StepVerifier;

@DataR2dbcTest
@ActiveProfiles("test")
@SpringJUnitConfig
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void testCountByTitleContaining() {
        StepVerifier.create(productRepository.countByTitleContaining("компьютер"))
            .expectNext(1L)
            .verifyComplete();
    }

    @Test
    void testCountByPriceGreaterThan() {
        StepVerifier.create(productRepository.countByPriceGreaterThan(300.0))
            .expectNext(2L)
            .verifyComplete();
    }

    @Test
    void testCountByPriceLessThan() {
        StepVerifier.create(productRepository.countByPriceLessThan(400.0))
            .expectNext(2L)
            .verifyComplete();
    }

    @Test
    void testCountAllProducts() {
        StepVerifier.create(productRepository.countByPriceLessThan(10000.0))
            .expectNext(4L)
            .verifyComplete();
    }

    @Test
    void testCountByTitleContaining_PartOfTitle() {
        StepVerifier.create(productRepository.countByTitleContaining("ет"))
            .expectNext(1L)
            .verifyComplete();
    }

    @Test
    void testCountByPriceBetween() {
        StepVerifier.create(productRepository.countByPriceBetween(300.0, 500.0))
            .expectNext(2L)
            .verifyComplete();
    }
}
