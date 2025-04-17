package com.shopservice.controllers;

import com.shopservice.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registerForm() {
        return "register";
    }

    @PostMapping("/register")
    public Mono<String> register(
        @RequestParam String username,
        @RequestParam String password,
        ServerWebExchange exchange) {

        return userService.registerUser(username, password)
            .thenReturn("redirect:/login?registered")
            .onErrorResume(e -> {
                exchange.getAttributes().put("error", e.getMessage());
                return Mono.just("redirect:/register?error");
            });
    }
}



