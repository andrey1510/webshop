package com.shopservice.controllers;

import com.shopservice.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private String validFormDataEncoded;
    private String invalidFormDataEncoded;

    @BeforeEach
    void setUp() {
        validFormDataEncoded = "username=testUser&password=validPassword";
        invalidFormDataEncoded = "username=testUser";
    }

    @Test
    void loginForm() {
        String result = userController.loginForm();
        assertEquals("login", result);
    }

    @Test
    void registerForm_() {
        String result = userController.registerForm();
        assertEquals("register", result);
    }

    @Test
    void login() {
        ServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.post("/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        );

        Mono<String> result = userController.login(exchange);

        StepVerifier.create(result)
            .expectNext("redirect:/products")
            .verifyComplete();
    }

    @Test
    void register() {
        when(userService.registerUser(anyString(), anyString()))
            .thenReturn(Mono.empty());

        ServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.post("/register")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(validFormDataEncoded)
        );

        Mono<String> result = userController.register(exchange);

        StepVerifier.create(result)
            .expectNext("redirect:/login?registered")
            .verifyComplete();

        verify(userService).registerUser("testUser", "validPassword");
    }

    @Test
    void register_NoPasswordError() {
        ServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.post("/register")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(invalidFormDataEncoded)
        );

        Mono<String> result = userController.register(exchange);

        StepVerifier.create(result)
            .expectNext("redirect:/register?error")
            .verifyComplete();

        assertEquals("Password is required", exchange.getAttributes().get("error"));
    }

}