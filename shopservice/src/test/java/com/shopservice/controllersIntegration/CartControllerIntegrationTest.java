package com.shopservice.controllersIntegration;

import com.shopservice.configs.TestDatabaseConfig;
import com.shopservice.services.PaymentService;
import org.mockito.Mock;
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
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
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

    @Mock
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("schema.sql"));
        populator.addScript(new ClassPathResource("test-data-full.sql"));
        databaseInitializer.setDatabasePopulator(populator);
        databaseInitializer.afterPropertiesSet();

        when(paymentService.checkFunds(anyDouble())).thenReturn(Mono.just(true));
        when(paymentService.processPayment(anyDouble())).thenReturn(Mono.just(true));

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
            .uri("/cart/remove")
            .body(fromFormData("productId", "6"))
            .exchange();

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
                assertTrue(response != null && response.contains("Ноутбук"));
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
        when(paymentService.processPayment(anyDouble())).thenReturn(Mono.just(true));

        webTestClient.post()
            .uri("/cart/checkout")
            .exchange()
            .expectStatus().is3xxRedirection()
            .expectHeader().valueEquals("Location", "/cart");

        webTestClient.get()
            .uri("/cart")
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class)
            .consumeWith(result -> {
                String response = result.getResponseBody();
                assertNotNull(response);
                assertFalse(response.isEmpty());
                assertTrue(response.contains("<html"));
                assertTrue(response.contains("Ноутбук") || response.contains("Смартфон"));
            });
    }

    @Test
    void testCheckout_InsufficientFunds() {

        when(paymentService.processPayment(anyDouble())).thenReturn(Mono.just(false));

        webTestClient.post()
            .uri("/cart/checkout")
            .exchange()
            .expectStatus().is3xxRedirection()
            .expectHeader().valueEquals("Location", "/cart");

        webTestClient.get()
            .uri("/cart")
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class)
            .consumeWith(result -> {
                String response = result.getResponseBody();
                assertTrue(response != null && response.contains("Ноутбук") && response.contains("Смартфон"));
            });
    }

    @Test
    void testCheckout_EmptyCart() {
        // Очистим корзину
        webTestClient.post()
            .uri("/cart/remove")
            .body(fromFormData("productId", "6"))
            .exchange();
        webTestClient.post()
            .uri("/cart/remove")
            .body(fromFormData("productId", "7"))
            .exchange();

        webTestClient.post()
            .uri("/cart/checkout")
            .exchange()
            .expectStatus().is3xxRedirection()
            .expectHeader().valueEquals("Location", "/cart");
    }

    @Test
    void testCheckout_PaymentServiceFails() {

        when(paymentService.processPayment(anyDouble()))
            .thenReturn(Mono.error(new RuntimeException("Payment service unavailable")));

        webTestClient.post()
            .uri("/cart/checkout")
            .exchange()
            .expectStatus().is3xxRedirection()
            .expectHeader().valueEquals("Location", "/cart");
    }

}