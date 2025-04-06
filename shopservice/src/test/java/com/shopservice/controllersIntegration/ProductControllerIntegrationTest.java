package com.shopservice.controllersIntegration;

import com.shopservice.configs.TestDatabaseConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureWebTestClient
@ActiveProfiles("testintegr")
@SpringJUnitConfig
@Import(TestDatabaseConfig.class)
class ProductControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ConnectionFactoryInitializer databaseInitializer;

    @BeforeEach
    void setUp() {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("schema.sql"));
        populator.addScript(new ClassPathResource("test-data-full.sql"));
        databaseInitializer.setDatabasePopulator(populator);
        databaseInitializer.afterPropertiesSet();
    }

    @Test
    void testGetProduct() {
        webTestClient.get()
            .uri("/products/6")
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class)
            .consumeWith(result -> {
                String response = result.getResponseBody();
                assertTrue(response != null && response.contains("product"));
            });
    }

    @Test
    void testGetProducts() {
        webTestClient.get()
            .uri("/products?page=0&size=2")
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class)
            .consumeWith(result -> {
                String response = result.getResponseBody();
                assertTrue(response != null && response.contains("products"));
            });
    }

    @Test
    void testGetProducts_WithTitleFilter() {
        webTestClient.get()
            .uri("/products?title=Ноутбук")
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class)
            .consumeWith(result -> {
                String response = result.getResponseBody();
                assertTrue(response != null &&
                    response.contains("Ноутбук") &&
                    !response.contains("Смартфон"));
            });
    }

    @Test
    void testGetProducts_WithPriceFilter() {
        webTestClient.get()
            .uri("/products?minPrice=400&maxPrice=600")
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class)
            .consumeWith(result -> {
                String response = result.getResponseBody();
                assertTrue(response != null &&
                    response.contains("Смартфон") &&
                    !response.contains("Планшет"));
            });
    }

    @Test
    void testGetProducts_WithSorting() {
        webTestClient.get()
            .uri("/products?sort=price-desc")
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class)
            .consumeWith(result -> {
                String response = result.getResponseBody();
                assertNotNull(response);
                int laptopPos = response.indexOf("Ноутбук");
                int phonePos = response.indexOf("Смартфон");
                assertTrue(laptopPos < phonePos);
            });
    }

}