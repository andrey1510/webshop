package com.shopservice.services;

import com.shopservice.entities.User;
import com.shopservice.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    public Mono<User> registerUser(String username, String rawPassword) {
        return userRepository.findByUsername(username)
            .switchIfEmpty(Mono.defer(() -> {
                User newUser = User.builder()
                    .username(username)
                    .password(passwordEncoder.encode(rawPassword))
                    .enabled(true)
                    .roles("ROLE_USER")
                    .build();
                return userRepository.save(newUser);
            }));
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