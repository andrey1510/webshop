package com.shopservice.configs;

import com.shopservice.generated.ApiClient;
import com.shopservice.generated.api.PaymentApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;

@Slf4j
@Configuration
public class WebClientConfiguration {
    @Bean
    public PaymentApi paymentApi(WebClient.Builder webClientBuilder) {
        WebClient webClient = webClientBuilder
            .baseUrl("http://localhost:8889")
            .filter((request, next) -> {
                log.info("Отправка запроса к PaymentService: {} {}", request.method(), request.url());
                return next.exchange(request);
            })
            .build();

        ApiClient apiClient = new ApiClient(webClient);
        return new PaymentApi(apiClient);
    }
}
