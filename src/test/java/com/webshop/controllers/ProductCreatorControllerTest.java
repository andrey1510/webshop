package com.webshop.controllers;

import com.webshop.dto.ProductInputDto;
import com.webshop.services.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProductCreatorControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductCreatorController productCreatorController;

    @Test
    void testShowForm() {

        String viewName = productCreatorController.showForm();

        assertEquals("product-creator", viewName);
    }

    @Test
    void testCreateProduct() {

        ProductInputDto productInputDto = ProductInputDto.builder()
            .title("Ноутбук")
            .description("описание ноутбука")
            .price(100.0)
            .build();

        MultipartFile image = new MockMultipartFile(
            "image", "test.jpg", "image/jpeg", "test image content".getBytes());
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

        String viewName = productCreatorController.createProduct(productInputDto, image, redirectAttributes);

        assertEquals("redirect:/product-creator", viewName);

        verify(productService, times(1)).createProduct(productInputDto);
        verify(redirectAttributes, times(1))
            .addFlashAttribute("successMessage", "Товар успешно создан!");
    }
}
