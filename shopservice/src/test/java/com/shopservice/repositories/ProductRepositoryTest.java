package com.shopservice.repositories;

import com.shopservice.configs.TestDatabaseConfig;
import com.shopservice.configs.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import reactor.test.StepVerifier;

@DataR2dbcTest
@ActiveProfiles("test")
@Import({TestDatabaseConfig.class, TestSecurityConfig.class})
@SpringJUnitConfig
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void countByTitleContaining() {
        StepVerifier.create(productRepository.countByTitleContaining("компьютер"))
            .expectNext(1L)
            .verifyComplete();
    }

    @Test
    void countByPriceGreaterThan() {
        StepVerifier.create(productRepository.countByPriceGreaterThan(300.0))
            .expectNext(2L)
            .verifyComplete();
    }

    @Test
    void countByPriceLessThan() {
        StepVerifier.create(productRepository.countByPriceLessThan(400.0))
            .expectNext(2L)
            .verifyComplete();
    }

    @Test
    void countAllProducts() {
        StepVerifier.create(productRepository.countByPriceLessThan(10000.0))
            .expectNext(4L)
            .verifyComplete();
    }

    @Test
    void countByTitleContaining_PartOfTitle() {
        StepVerifier.create(productRepository.countByTitleContaining("ет"))
            .expectNext(1L)
            .verifyComplete();
    }

    @Test
    void countByPriceBetween() {
        StepVerifier.create(productRepository.countByPriceBetween(300.0, 500.0))
            .expectNext(2L)
            .verifyComplete();
    }
}
