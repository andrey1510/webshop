package com.paymentservice.controllersIntegration;

import com.paymentservice.configs.TestDatabaseConfig;
import com.paymentservice.dto.PaymentRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest
@AutoConfigureWebTestClient
@ActiveProfiles("test")
@Import(TestDatabaseConfig.class)
@SpringJUnitConfig
class PaymentControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ConnectionFactoryInitializer databaseInitializer;

    @BeforeEach
    void setUp() {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("schema.sql"));
        populator.addScript(new ClassPathResource("data.sql"));
        databaseInitializer.setDatabasePopulator(populator);
        databaseInitializer.afterPropertiesSet();
    }

    @Test
    void testCheckFunds_SufficientBalance() {
        int userId = 1;
        double amount = 500.0;

        webTestClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/check")
                .queryParam("id", userId)
                .queryParam("amount", amount)
                .build())
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(userId)
            .jsonPath("$.isBalanceSufficient").isEqualTo(true);
    }

    @Test
    void testCheckFunds_InsufficientBalance() {
        int userId = 2;
        double amount = 1500.0;

        webTestClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/check")
                .queryParam("id", userId)
                .queryParam("amount", amount)
                .build())
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(userId)
            .jsonPath("$.isBalanceSufficient").isEqualTo(false);
    }

    @Test
    void testProcessPayment_Success() {
        PaymentRequest request = new PaymentRequest()
            .id(3)
            .amount(300.0);

        webTestClient.post()
            .uri("/pay")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(request.getId())
            .jsonPath("$.isBalanceSufficient").isEqualTo(true);
    }

    @Test
    void testProcessPayment_InsufficientBalance() {
        PaymentRequest request = new PaymentRequest()
            .id(1)
            .amount(15000.0);

        webTestClient.post()
            .uri("/pay")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isBadRequest()
            .expectBody()
            .jsonPath("$.id").isEqualTo(request.getId())
            .jsonPath("$.isBalanceSufficient").isEqualTo(false);
    }
}