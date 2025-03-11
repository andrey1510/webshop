package com.webshop.services;

import com.webshop.dto.ProductInputDto;
import com.webshop.dto.ProductPreviewDto;
import com.webshop.entities.Product;
import com.webshop.exceptions.ProductNotFoundException;
import com.webshop.repositories.ProductRepository;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private ProductInputDto productInputDto;
    private Product product;
    private Page<ProductPreviewDto> productPreviewPage;

    @BeforeEach
    @SneakyThrows
    public void setUp() {

        ReflectionTestUtils.setField(productService, "uploadDirectory", "uploads");

        productInputDto = ProductInputDto.builder()
            .title("Ноутбук")
            .description("описание ноутбука")
            .price(1000.0)
            .image(null)
            .build();

        product = Product.builder()
            .id(1)
            .title("Ноутбук")
            .description("описание ноутбука")
            .price(1000.0)
            .imagePath(null)
            .build();

        productPreviewPage = new PageImpl<>(Collections.singletonList(
            new ProductPreviewDto(1, "Ноутбук", 1000.0, "laptop.jpg")
        ));
    }

    @Test
    public void testCreateProduct_WithoutImage() {

        when(productRepository.save(any(Product.class))).thenReturn(product);

        Product result = productService.createProduct(productInputDto);

        assertNotNull(result);
        assertEquals("Ноутбук", result.getTitle());
        assertEquals("описание ноутбука", result.getDescription());
        assertEquals(1000.0, result.getPrice());
        assertNull(result.getImagePath());

        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    public void testCreateProduct_WithImage() {

        MultipartFile image = mock(MultipartFile.class);
        when(image.getOriginalFilename()).thenReturn("laptop.jpg");
        when(image.isEmpty()).thenReturn(false);
        productInputDto.setImage(image);

        when(productRepository.save(any(Product.class))).thenReturn(product);

        Product result = productService.createProduct(productInputDto);

        assertNotNull(result);
        assertEquals("Ноутбук", result.getTitle());
        assertNotEquals("laptop.jpg", result.getImagePath());

        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    public void testGetProductById_Success() {

        when(productRepository.findById(1)).thenReturn(Optional.of(product));

        Product result = productService.getProductById(1);

        assertNotNull(result);
        assertEquals("Ноутбук", result.getTitle());
        assertEquals(1000.0, result.getPrice());

        verify(productRepository, times(1)).findById(1);
    }

    @Test
    public void testGetProductById_NotFound() {

        when(productRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.getProductById(1));

        verify(productRepository, times(1)).findById(1);
    }

    @Test
    public void testGetProductPreviewDtos_FilterByTitle() {

        when(productRepository.findProductPreviewDtosByTitleContaining(eq("ноут"), any(Pageable.class)))
            .thenReturn(productPreviewPage);

        Page<ProductPreviewDto> result = productService
            .getProductPreviewDtos("ноут", null, null, null, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Ноутбук", result.getContent().get(0).title());

        verify(productRepository, times(1))
            .findProductPreviewDtosByTitleContaining(eq("ноут"), any(Pageable.class));
    }

    @Test
    public void testGetProductPreviewDtos_FilterByPriceRange() {

        when(productRepository
            .findProductPreviewDtosByPriceBetween(eq(500.0), eq(1000.0), any(Pageable.class)))
            .thenReturn(productPreviewPage);

        Page<ProductPreviewDto> result = productService
            .getProductPreviewDtos(null, 500.0, 1000.0, null, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Ноутбук", result.getContent().get(0).title());

        verify(productRepository, times(1))
            .findProductPreviewDtosByPriceBetween(eq(500.0), eq(1000.0), any(Pageable.class));
    }

    @Test
    public void testGetProductPreviewDtos_SortByPriceDesc() {
        when(productRepository.findAllProductPreviewDtos(any(Pageable.class))).thenReturn(productPreviewPage);

        Page<ProductPreviewDto> result = productService
            .getProductPreviewDtos(null, null, null, "price-desc", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Ноутбук", result.getContent().get(0).title());

        verify(productRepository, times(1)).findAllProductPreviewDtos(any(Pageable.class));
    }

    @Test
    public void testGetProductPreviewDtos_SortByTitleDesc() {

        when(productRepository.findAllProductPreviewDtos(any(Pageable.class)))
            .thenReturn(productPreviewPage);

        Page<ProductPreviewDto> result = productService
            .getProductPreviewDtos(null, null, null, "title-desc", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Ноутбук", result.getContent().get(0).title());

        verify(productRepository, times(1))
            .findAllProductPreviewDtos(argThat(pageable -> {
                Sort sort = pageable.getSort();
                return sort.getOrderFor("title").getDirection().isDescending();
            }));
    }

    @Test
    public void testGetProductPreviewDtos_SortByPriceAsc() {

        when(productRepository.findAllProductPreviewDtos(any(Pageable.class)))
            .thenReturn(productPreviewPage);

        Page<ProductPreviewDto> result = productService
            .getProductPreviewDtos(null, null, null, "price-asc", 0, 10);

        // Проверки
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Ноутбук", result.getContent().get(0).title());

        verify(productRepository, times(1))
            .findAllProductPreviewDtos(argThat(pageable -> {
                Sort sort = pageable.getSort();
                return sort.getOrderFor("price").getDirection().isAscending();
            }));
    }

}
