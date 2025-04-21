package com.shopservice.configs;

import com.shopservice.generated.ApiClient;
import com.shopservice.generated.api.PaymentApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.server.WebSessionServerOAuth2AuthorizedClientRepository;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Configuration
public class WebClientConfiguration {

    @Value("${paymentservice-url}")
    private String paymentServiceUrl;

    @Bean
    public PaymentApi paymentApi(WebClient.Builder webClientBuilder,
                                 ReactiveOAuth2AuthorizedClientManager clientManager) {

        ServerOAuth2AuthorizedClientExchangeFilterFunction oauthFilter =
            new ServerOAuth2AuthorizedClientExchangeFilterFunction(clientManager);
        oauthFilter.setDefaultClientRegistrationId("webshop-client");

        WebClient client = webClientBuilder
            .baseUrl(paymentServiceUrl)
            .filter(oauthFilter)
            .filter((request, next) -> {
                log.info("Sending request to {} {}", request.method(), request.url());
                return next.exchange(request);
            })
            .build();

        ApiClient apiClient = new ApiClient(client);
        apiClient.setBasePath(paymentServiceUrl);
        return new PaymentApi(apiClient);
    }

    @Bean
    public ReactiveOAuth2AuthorizedClientManager authorizedClientManager(
        ReactiveClientRegistrationRepository clientRegistrations,
        ServerOAuth2AuthorizedClientRepository authorizedClientRepository) {

        ReactiveOAuth2AuthorizedClientProvider authorizedClientProvider =
            ReactiveOAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials()
                .build();

        DefaultReactiveOAuth2AuthorizedClientManager authorizedClientManager =
            new DefaultReactiveOAuth2AuthorizedClientManager(
                clientRegistrations,
                authorizedClientRepository);

        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);
        return authorizedClientManager;
    }

    @Bean
    public ServerOAuth2AuthorizedClientRepository authorizedClientRepository() {
        return new WebSessionServerOAuth2AuthorizedClientRepository();
    }
}
