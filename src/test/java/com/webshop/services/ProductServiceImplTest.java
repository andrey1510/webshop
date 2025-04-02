package com.webshop.services;

import com.webshop.dto.ProductInputDto;
import com.webshop.dto.ProductPreviewDto;
import com.webshop.entities.Product;
import com.webshop.exceptions.ProductNotFoundException;
import com.webshop.repositories.ProductPreviewDtoRepository;
import com.webshop.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductPreviewDtoRepository productPreviewDtoRepository;

    @Mock
    private FilePart filePart;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product testProduct;
    private Product savedProduct;
    private ProductPreviewDto testPreviewDto;
    private ProductInputDto inputDto;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
            .id(1)
            .title("Test Product")
            .description("Test Description")
            .price(100.0)
            .imagePath("test.jpg")
            .build();
        testPreviewDto = new ProductPreviewDto(
            1,
            "Test Product",
            100.0,
            "test.jpg"
        );
        inputDto = new ProductInputDto(
            "New Product",
            "New Description",
            200.0,
            filePart
        );
        savedProduct = Product.builder()
            .id(1)
            .title("New Product")
            .description("New Description")
            .price(200.0)
            .imagePath("noimage.png")
            .build();
    }

    @Test
    void testGetProductById() {
        when(productRepository.findById(anyInt())).thenReturn(Mono.just(testProduct));

        StepVerifier.create(productService.getProductById(1))
            .expectNextMatches(product ->
                product.getId().equals(1) &&
                    product.getTitle().equals("Test Product") &&
                    product.getPrice().equals(100.0))
            .verifyComplete();

        verify(productRepository).findById(1);
    }

    @Test
    void testGetProductById_ProductNotExists() {
        when(productRepository.findById(anyInt())).thenReturn(Mono.empty());

        StepVerifier.create(productService.getProductById(999))
            .expectError(ProductNotFoundException.class)
            .verify();

        verify(productRepository).findById(999);
    }

    @Test
    void testGetPageableProductPreviewDtos_WithTitleFilter() {

        when(productRepository.countByTitleContaining(anyString())).thenReturn(Mono.just(1L));
        when(productPreviewDtoRepository.findProductPreviewDtosByTitleContaining(anyString(), any(Pageable.class)))
            .thenReturn(Flux.just(testPreviewDto));

        StepVerifier.create(productService.getPageableProductPreviewDtos(
            "Test", null, null, "title-asc", 0, 10))
            .expectNextMatches(page ->
                page.getContent().size() == 1 &&
                    page.getContent().get(0).title().equals("Test Product") &&
                    page.getTotalElements() == 1)
            .verifyComplete();

        verify(productRepository).countByTitleContaining("Test");
        verify(productPreviewDtoRepository)
            .findProductPreviewDtosByTitleContaining(eq("Test"), any(Pageable.class));
    }

    @Test
    void testGetPageableProductPreviewDtos_WithPriceRange() {

        when(productRepository.countByPriceBetween(anyDouble(), anyDouble())).thenReturn(Mono.just(1L));
        when(productPreviewDtoRepository
            .findProductPreviewDtosByPriceBetween(anyDouble(), anyDouble(), any(Pageable.class)))
            .thenReturn(Flux.just(testPreviewDto));

        StepVerifier.create(productService.getPageableProductPreviewDtos(
            null, 50.0, 150.0, "price-asc", 0, 10))
            .expectNextMatches(page ->
                page.getContent().size() == 1 &&
                    page.getContent().get(0).price().equals(100.0) &&
                    page.getTotalElements() == 1)
            .verifyComplete();

        verify(productRepository).countByPriceBetween(50.0, 150.0);
        verify(productPreviewDtoRepository)
            .findProductPreviewDtosByPriceBetween(eq(50.0), eq(150.0), any(Pageable.class));
    }

    @Test
    void testGetPageableProductPreviewDtos_NoFilters() {

        when(productRepository.count()).thenReturn(Mono.just(1L));
        when(productPreviewDtoRepository.findAllProductPreviewDtos(any(Pageable.class)))
            .thenReturn(Flux.just(testPreviewDto));

        StepVerifier.create(productService.getPageableProductPreviewDtos(
            null, null, null, "title-asc", 0, 10))
            .expectNextMatches(page ->
                page.getContent().size() == 1 &&
                    page.getTotalElements() == 1)
            .verifyComplete();

        verify(productRepository).count();
        verify(productPreviewDtoRepository).findAllProductPreviewDtos(any(Pageable.class));
    }

    @Test
    void testCreateProduct() {

        when(filePart.filename()).thenReturn("invalid.exe");
        when(productRepository.save(any())).thenReturn(Mono.just(savedProduct));

        StepVerifier.create(productService.createProduct(inputDto))
            .expectNextMatches(product ->
                product.getImagePath().equals("noimage.png"))
            .verifyComplete();

        verify(productRepository).save(any());
    }

}