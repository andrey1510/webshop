package com.shopservice.controllers;

import com.shopservice.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


@Slf4j
@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Mono<String> login(ServerWebExchange exchange) {
        return exchange.getFormData()
            .doOnNext(formData -> {
                String username = formData.getFirst("username");
                log.info("Login attempt for user: {}", username);
            })
            .thenReturn("redirect:/products");
    }

    @GetMapping("/register")
    public String registerForm() {
        return "register";
    }

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Mono<String> register(ServerWebExchange exchange) {
        return exchange.getFormData()
            .flatMap(formData -> {
                String username = formData.getFirst("username");
                String password = formData.getFirst("password");

                if (password == null) {
                    exchange.getAttributes().put("error", "Password is required");
                    return Mono.just("redirect:/register?error");
                }

                return userService.registerUser(username, password)
                    .thenReturn("redirect:/login?registered")
                    .onErrorResume(e -> {
                        log.error("Registration failed", e);
                        exchange.getAttributes().put("error", e.getMessage());
                        return Mono.just("redirect:/register?error");
                    });
            });
    }

}



