package com.webshop.controllersIntegration;

import com.webshop.configs.TestDatabaseConfig;
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

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureWebTestClient
@ActiveProfiles("testintegr")
@SpringJUnitConfig
@Import(TestDatabaseConfig.class)
class OrderControllerIntegrationTest {

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
    void testGetCompletedOrder() {
        webTestClient.get()
            .uri("/orders/7")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .consumeWith(result -> {
                String response = new String(result.getResponseBody());
                assertTrue(response.contains("Заказ № 7"));
            });
    }

    @Test
    void testGetCompletedOrder_WrongId() {
        webTestClient.get()
            .uri("/orders/999")
            .exchange()
            .expectStatus().isNotFound();
    }

    @Test
    void testGetAllCompletedOrders() {
        webTestClient.get()
            .uri("/orders")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .consumeWith(result -> {
                String response = new String(result.getResponseBody());
                assertTrue(response.contains("Заказ № 7"));
                assertTrue(response.contains("Заказ № 8"));
            });
    }

    @Test
    void completeOrder_ShouldMoveCartToCompleted() {
        webTestClient.post()
            .uri("/orders/complete")
            .exchange();
        webTestClient.get()
            .uri("/orders")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .consumeWith(result -> {
                String response = new String(result.getResponseBody());
                assertTrue(response.contains("Заказ № 7"));
            });
    }

}
