package com.shopservice.configs;

import com.shopservice.repositories.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.header.ReferrerPolicyServerHttpHeadersWriter;
import reactor.core.publisher.Mono;

import java.net.URI;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfiguration {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            .authorizeExchange(exchanges -> exchanges
                .pathMatchers("/", "/users", "/login", "/register", "/static/**", "/register", "/products", "/products/**", "/product-creator").permitAll()
                .pathMatchers("/cart/**", "/orders/**").hasRole("USER")
                .anyExchange().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .authenticationSuccessHandler((exchange, authentication) -> {
                    exchange.getExchange().getResponse().setStatusCode(HttpStatus.FOUND);
                    exchange.getExchange().getResponse().getHeaders().setLocation(URI.create("/products"));
                    return Mono.empty();
                })
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessHandler((exchange, authentication) -> {
                    exchange.getExchange().getResponse().setStatusCode(HttpStatus.FOUND);
                    exchange.getExchange().getResponse().getHeaders().setLocation(URI.create("/products"));
                    return Mono.empty();
                })
            )
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.disable())
                .referrerPolicy(
                    referrer -> referrer.policy(ReferrerPolicyServerHttpHeadersWriter.ReferrerPolicy.SAME_ORIGIN)))
            .exceptionHandling(handling -> handling
                .authenticationEntryPoint((exchange, e) ->
                    Mono.fromRunnable(() -> {
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    })
                )
            )
            .build();
    }

    @Bean
    public ReactiveUserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> userRepository.findByUsername(username)
            .map(user -> User.withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRoles().split(","))
                .build()
            );
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
