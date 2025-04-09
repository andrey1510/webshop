package com.shopservice.configs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Configuration
public class WebClientConfiguration {
    @Bean
    public WebClient paymentServiceWebClient() {
        return WebClient.builder()
            .baseUrl("http://localhost:8889")
            .filter((request, next) -> {
                log.info("Отправка запроса к PaymentService: {} {}", request.method(), request.url());
                return next.exchange(request);
            })
            .build();
    }
}
