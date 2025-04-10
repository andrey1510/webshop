package com.shopservice.configs;

import com.shopservice.generated.ApiClient;
import com.shopservice.generated.api.PaymentApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Configuration
public class WebClientConfiguration {

    @Value("${paymentservice-url}")
    private String paymentServiceUrl;

    @Bean
    public PaymentApi paymentApi(WebClient.Builder webClientBuilder) {
        ApiClient apiClient = new ApiClient(buildWebClient(webClientBuilder, paymentServiceUrl));
        apiClient.setBasePath(paymentServiceUrl);

        return new PaymentApi(apiClient);
    }

    private WebClient buildWebClient(WebClient.Builder builder, String baseUrl) {
        return builder
            .baseUrl(baseUrl)
            .filter((request, next) -> {
                log.info("Отправка запроса к PaymentService: {} {}", request.method(), request.url());
                return next.exchange(request);
            })
            .build();
    }
}
