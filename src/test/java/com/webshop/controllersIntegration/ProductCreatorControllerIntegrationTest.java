package com.webshop.controllersIntegration;

import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class ProductCreatorControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @AfterEach
    @SneakyThrows
    void tearDown() {
        Path uploadDir = Paths.get("build/tmp/uploads");
        if (Files.exists(uploadDir)) {
            Files.walk(uploadDir)
                .sorted(Comparator.reverseOrder())
                .forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (Exception e) {
                        throw new RuntimeException("Ошибка при удалении файла: " + path, e);
                    }
                });
        }
    }

    @Test
    void testShowForm() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/product-creator"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(view().name("product-creator"));
    }

    @Test
    @SneakyThrows
    void testCreateProduct() {
        MockMultipartFile image = new MockMultipartFile(
            "image",
            "test.jpg",
            "image/jpeg",
            "test image content".getBytes()
        );

        mockMvc.perform(MockMvcRequestBuilders.multipart("/product-creator")
                .file(image)
                .param("title", "Ноутбук")
                .param("description", "описание ноутбука")
                .param("price", "100.0"))
            .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
            .andExpect(redirectedUrl("/product-creator"))
            .andExpect(flash().attributeExists("successMessage"))
            .andExpect(flash().attribute("successMessage", "Товар успешно создан!"));
    }
}
