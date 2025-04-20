package com.shopservice.repositories;

import com.shopservice.configs.TestDatabaseConfig;
import com.shopservice.configs.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

@DataR2dbcTest
@ActiveProfiles("test")
@Import({ProductPreviewDtoRepository.class, TestDatabaseConfig.class, TestSecurityConfig.class})
class ProductPreviewDtoRepositoryTest {

    @Autowired
    private ProductPreviewDtoRepository repository;

    private final Pageable defaultPageable = PageRequest.of(0, 10);

    @Test
    void testFindAllProductPreviewDtos() {
        StepVerifier.create(repository.findAllProductPreviewDtos(defaultPageable).collectList())
            .expectNextMatches(list -> list.size() == 4 &&
                list.stream().anyMatch(dto -> dto.title().equals("Ноутбук")) &&
                list.stream().anyMatch(dto -> dto.title().equals("Смартфон")) &&
                list.stream().anyMatch(dto -> dto.title().equals("Планшет")) &&
                list.stream().anyMatch(dto -> dto.title().equals("Персональный компьютер")))
            .verifyComplete();
    }

    @Test
    void testFindProductPreviewDtosByPriceGreaterThan() {
        StepVerifier.create(repository.findProductPreviewDtosByPriceGreaterThan(400.0, defaultPageable).collectList())
            .expectNextMatches(list -> list.size() == 2 &&
                list.stream().anyMatch(dto -> dto.title().equals("Ноутбук")) &&
                list.stream().anyMatch(dto -> dto.title().equals("Смартфон")))
            .verifyComplete();
    }

    @Test
    void testFindProductPreviewDtosByPriceLessThan() {
        StepVerifier.create(repository.findProductPreviewDtosByPriceLessThan(400.0, defaultPageable)
                .collectList())
            .expectNextMatches(list -> list.size() == 2 &&
                list.stream().anyMatch(dto -> dto.title().equals("Планшет")) &&
                list.stream().anyMatch(dto -> dto.title().equals("Персональный компьютер")))
            .verifyComplete();
    }

    @Test
    void testFindProductPreviewDtosByPriceBetween() {
        StepVerifier.create(repository
                .findProductPreviewDtosByPriceBetween(200.0, 500.0, defaultPageable).collectList())
            .expectNextMatches(list -> list.size() == 3 &&
                list.stream().anyMatch(dto -> dto.title().equals("Смартфон")) &&
                list.stream().anyMatch(dto -> dto.title().equals("Планшет")) &&
                list.stream().anyMatch(dto -> dto.title().equals("Персональный компьютер")))
            .verifyComplete();
    }

}