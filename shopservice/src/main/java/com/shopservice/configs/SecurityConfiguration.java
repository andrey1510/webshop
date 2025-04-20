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
import reactor.core.publisher.Mono;

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
                .authenticationSuccessHandler((exchange, authentication) ->
                    Mono.empty()
                )
                .authenticationFailureHandler((exchange, e) ->
                    Mono.error(e)
                )
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessHandler((exchange, authentication) ->
                    Mono.empty()
                )
            )
            .csrf(csrf -> csrf.disable())
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
