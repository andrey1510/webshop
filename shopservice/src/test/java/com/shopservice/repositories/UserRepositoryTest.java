package com.shopservice.repositories;

import com.shopservice.configs.TestDatabaseConfig;
import com.shopservice.configs.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataR2dbcTest
@ActiveProfiles("test")
@Import({TestDatabaseConfig.class, TestSecurityConfig.class})
@SpringJUnitConfig
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByUsername_WhenUserExists_ShouldReturnUser() {
        StepVerifier.create(userRepository.findByUsername("test-user"))
            .assertNext(user -> {
                assertEquals(101, user.getId());
                assertEquals("test-user", user.getUsername());
                assertEquals("password", user.getPassword());
                assertTrue(user.isEnabled());
                assertEquals("USER", user.getRoles());
            })
            .verifyComplete();
    }

    @Test
    void findByUsername_WhenUserNotExists_ShouldReturnEmpty() {
        StepVerifier.create(userRepository.findByUsername("non-existent-user"))
            .expectNextCount(0)
            .verifyComplete();
    }
}
