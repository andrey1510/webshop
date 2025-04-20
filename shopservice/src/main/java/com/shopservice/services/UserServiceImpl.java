package com.shopservice.services;

import com.shopservice.entities.User;
import com.shopservice.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    public Mono<User> registerUser(String username, String rawPassword) {
        log.info("Attempting to register user: {}", username);

        if (username == null || username.isBlank()) {
            return Mono.error(new IllegalArgumentException("Username cannot be empty"));
        }
        if (rawPassword == null || rawPassword.isBlank()) {
            return Mono.error(new IllegalArgumentException("Password cannot be empty"));
        }

        return userRepository.findByUsername(username)
            .flatMap(existing -> {
                log.warn("User already exists: {}", username);
                return Mono.error(new IllegalArgumentException("User already exists"));
            })
            .switchIfEmpty(Mono.defer(() -> {
                User newUser = User.builder()
                    .username(username.trim())
                    .password(passwordEncoder.encode(rawPassword))
                    .enabled(true)
                    .roles("USER")
                    .build();
                return userRepository.save(newUser);
            }))
            .cast(User.class);
    }
    @Override
    public Mono<Integer> getCurrentUserId() {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .flatMap(auth -> {
                String username = auth.getName();
                return userRepository.findByUsername(username)
                    .map(User::getId);
            });
    }

}