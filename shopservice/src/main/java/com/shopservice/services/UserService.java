package com.shopservice.services;

import com.shopservice.entities.User;
import reactor.core.publisher.Mono;

public interface UserService {
    Mono<User> registerUser(String username, String rawPassword);

    Mono<Integer> getCurrentUserId();
}
