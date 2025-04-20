package com.shopservice.repositories;

import com.shopservice.configs.TestDatabaseConfig;
import com.shopservice.configs.TestSecurityConfig;
import com.shopservice.entities.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import java.util.List;

@DataR2dbcTest
@ActiveProfiles("test")
@Import({OrderItemProductRepository.class, TestDatabaseConfig.class, TestSecurityConfig.class})
class OrderItemProductRepositoryTest {

    @Autowired
    private DatabaseClient databaseClient;

    private OrderItemProductRepository repository;

    @BeforeEach
    void setUp() {
        repository = new OrderItemProductRepository(databaseClient);
    }

    @Test
    void testFindByCustomerOrderIdWithProduct() {
        StepVerifier.create(repository.findByCustomerOrderIdWithProduct(8).collectList())
            .expectNextMatches(items -> items.stream()
                .filter(item -> item.getProductId() == 8)
                .findFirst()
                .map(item -> {
                    Product product = item.getProduct();
                    return product != null &&
                        product.getTitle().equals("Планшет") &&
                        product.getPrice() == 300.0;
                })
                .orElse(false))
            .verifyComplete();
    }

    @Test
    void testFindByCustomerOrderIdWithProduct_CheckQuantities() {
        StepVerifier.create(repository.findByCustomerOrderIdWithProduct(6).collectList())
            .expectNextMatches(items -> items.stream()
                .filter(item -> item.getProductId() == 6)
                .findFirst()
                .map(item -> item.getQuantity() == 1)
                .orElse(false) &&
                items.stream()
                    .filter(item -> item.getProductId() == 7)
                    .findFirst()
                    .map(item -> item.getQuantity() == 2)
                    .orElse(false))
            .verifyComplete();
    }

    @Test
    void testFindByCustomerOrderIdWithProduct_OrderNotExists() {
        StepVerifier.create(repository.findByCustomerOrderIdWithProduct(999).collectList())
            .expectNextMatches(List::isEmpty)
            .verifyComplete();
    }

}