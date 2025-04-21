package com.shopservice.utils;

import com.shopservice.entities.User;
import com.shopservice.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        userRepository.deleteAll()
            .thenMany(
                Flux.just(
                        User.builder()
                            .username("sample-user-1")
                            .password(passwordEncoder.encode("password-1"))
                            .enabled(true)
                            .roles("USER")
                            .build(),
                        User.builder()
                            .username("sample-user-2")
                            .password(passwordEncoder.encode("password-2"))
                            .enabled(true)
                            .roles("USER")
                            .build()
                    )
                    .flatMap(userRepository::save)
            )
            .thenMany(userRepository.findAll())
            .subscribe(user -> log.info("Пользователь зарегистрирован: {}", user));
    }
}
