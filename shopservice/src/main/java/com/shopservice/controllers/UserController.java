package com.shopservice.controllers;

import com.shopservice.repositories.UserRepository;
import com.shopservice.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    private final UserRepository userRepository;

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

        exchange.getRequest().getHeaders().forEach((k, v) ->
            log.debug("Header: {} = {}", k, v));

        return exchange.getFormData()
            .flatMap(formData -> {
                String username = formData.getFirst("username");
                String password = formData.getFirst("password");

                log.info("Registering user: {}", username);

                if (password == null) {
                    log.error("Password is null in form data");
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


    @GetMapping("/users")
    public Mono<Void> listAllUsers() {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String testRawPassword = "password";

        return userRepository.findAll()
            .collectList()
            .doOnNext(users -> {
                System.out.println("\n=== Список всех пользователей ===");
                System.out.println("Тестовый пароль для проверки: " + testRawPassword);
                System.out.println("--------------------------------");

                users.forEach(user -> {
                    boolean passwordMatches = passwordEncoder.matches(testRawPassword, user.getPassword());

                    System.out.printf(
                        "ID: %d | Username: %s | Roles: %s | Enabled: %s%n" +
                            "Encoded password: %s%n" +
                            "Matches test password: %s%n" +
                            "--------------------------------%n",
                        user.getId(),
                        user.getUsername(),
                        user.getRoles(),
                        user.isEnabled() ? "Да" : "Нет",
                        user.getPassword(),
                        passwordMatches ? "ДА" : "НЕТ"
                    );
                });
                System.out.println("==============================\n");
            })
            .then(Mono.empty());
    }
}



