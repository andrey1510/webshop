package com.shopservice.controllersIntegration;

import com.shopservice.configs.TestDatabaseConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.web.reactive.function.BodyInserters.fromFormData;

@SpringBootTest
@AutoConfigureWebTestClient
@ActiveProfiles("testintegr")
@SpringJUnitConfig
@Import(TestDatabaseConfig.class)
class CartControllerIntegrationTest {

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
    void testGetCart() {
        webTestClient.get()
            .uri("/cart")
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class)
            .consumeWith(result -> {
                String response = result.getResponseBody();
                assertTrue(response != null &&
                    response.contains("Ноутбук"));
            });
    }

    @Test
    void testAddItemToCart() {
        webTestClient.post()
            .uri("/cart/add?productId=6")
            .header(HttpHeaders.REFERER, "/products/6")
            .exchange()
            .expectStatus().is3xxRedirection()
            .expectHeader().valueEquals("Location", "/products/6");
        webTestClient.get()
            .uri("/cart")
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class)
            .consumeWith(result -> {
                String response = result.getResponseBody();
                assertTrue(response != null &&
                    response.contains("Ноутбук") &&
                    response.contains("1000.0"));
            });
    }

    @Test
    void testUpdateItemQuantity() {
        webTestClient.post()
            .uri("/cart/update")
            .body(fromFormData("productId", "7")
                .with("quantity", "3"))
            .header(HttpHeaders.REFERER, "/cart")
            .exchange();
        webTestClient.get()
            .uri("/cart")
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class)
            .consumeWith(result -> {
                String response = result.getResponseBody();
                assertTrue(response != null &&
                    response.contains("Смартфон") &&
                    response.contains("3"));
            });
    }

    @Test
    void testRemoveItemFromCart() {
        webTestClient.post()
            .uri("/cart/remove")
            .body(fromFormData("productId", "7"))
            .header(HttpHeaders.REFERER, "/cart")
            .exchange();
        webTestClient.get()
            .uri("/cart")
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class)
            .consumeWith(result -> {
                String response = result.getResponseBody();
                assertTrue(response != null && !response.contains("Планшет"));
            });
    }

    @Test
    void testCheckout() {
        webTestClient.post()
            .uri("/cart/checkout")
            .exchange()
            .expectHeader().valueMatches("Location", "/orders/\\d+");
        webTestClient.get()
            .uri("/cart")
            .exchange()
            .expectStatus().isOk();
    }

}