package com.shopservice.services;

import com.shopservice.entities.User;
import com.shopservice.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void registerUser() {

        String username = "testuser";
        String rawPassword = "password";
        String encodedPassword = "encodedPassword";

        User newUser = User.builder()
            .username(username)
            .password(encodedPassword)
            .enabled(true)
            .roles("USER")
            .build();

        when(userRepository.findByUsername(username)).thenReturn(Mono.empty());
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(newUser));

        StepVerifier.create(userService.registerUser(username, rawPassword))
            .expectNextMatches(user ->
                user.getUsername().equals(username) &&
                    user.getPassword().equals(encodedPassword) &&
                    user.isEnabled() &&
                    user.getRoles().equals("USER"))
            .verifyComplete();

        verify(userRepository).findByUsername(username);
        verify(passwordEncoder).encode(rawPassword);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_ExistingUserError() {

        String username = "existinguser";
        String rawPassword = "password";
        User existingUser = User.builder().username(username).build();

        when(userRepository.findByUsername(username)).thenReturn(Mono.just(existingUser));

        StepVerifier.create(userService.registerUser(username, rawPassword))
            .expectErrorMatches(ex ->
                ex instanceof IllegalArgumentException &&
                    ex.getMessage().equals("User already exists"))
            .verify();

        verify(userRepository).findByUsername(username);
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void registerUser_EmptyUsernameError() {

        StepVerifier.create(userService.registerUser("", "password"))
            .expectErrorMatches(ex ->
                ex instanceof IllegalArgumentException &&
                    ex.getMessage().equals("Username cannot be empty"))
            .verify();

        verifyNoInteractions(userRepository);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void registerUser_EmptyPasswordError() {

        StepVerifier.create(userService.registerUser("testuser", ""))
            .expectErrorMatches(ex ->
                ex instanceof IllegalArgumentException &&
                    ex.getMessage().equals("Password cannot be empty"))
            .verify();

        verifyNoInteractions(userRepository);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void getCurrentUserId() {

        String username = "testuser";
        int userId = 1;
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(authentication.getName()).thenReturn(username);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        User user = User.builder().id(userId).username(username).build();
        when(userRepository.findByUsername(username)).thenReturn(Mono.just(user));

        StepVerifier.create(
                Mono.deferContextual(contextView ->
                    userService.getCurrentUserId()
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)))
                )
            )
            .expectNext(userId)
            .verifyComplete();

        verify(userRepository).findByUsername(username);
    }

    @Test
    void getCurrentUserId_NonExistentUserError() {

        String username = "nonexistent";
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(authentication.getName()).thenReturn(username);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(userRepository.findByUsername(username)).thenReturn(Mono.empty());

        StepVerifier.create(
                Mono.deferContextual(contextView ->
                    userService.getCurrentUserId()
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)))
                )
            )
            .expectNextCount(0)
            .verifyComplete();

        verify(userRepository).findByUsername(username);
    }
}
