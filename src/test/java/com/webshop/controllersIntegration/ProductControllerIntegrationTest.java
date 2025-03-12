package com.webshop.controllersIntegration;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ActiveProfiles("test")
@Sql("/test-data-products.sql")
@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetProduct() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/products/6"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(view().name("product"))
            .andExpect(model().attributeExists("product"))
            .andExpect(model().attributeExists("cartProductQuantity"));
    }

    @Test
    void testGetProducts() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/products")
                .param("page", "0")
                .param("size", "4")
                .param("title", "Ноутбук")
                .param("minPrice", "500")
                .param("maxPrice", "1500")
                .param("sort", "asc"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(view().name("products"))
            .andExpect(model().attributeExists("products"))
            .andExpect(model().attributeExists("currentPage"))
            .andExpect(model().attributeExists("pageSize"))
            .andExpect(model().attributeExists("totalPages"))
            .andExpect(model().attributeExists("title"))
            .andExpect(model().attributeExists("minPrice"))
            .andExpect(model().attributeExists("maxPrice"))
            .andExpect(model().attributeExists("sort"))
            .andExpect(model().attributeExists("cartProductsQuantities"));
    }

    @Test
    @SneakyThrows
    void testHandleProductNotFoundException() {
        mockMvc.perform(MockMvcRequestBuilders.get("/products/999"))
            .andExpect(MockMvcResultMatchers.status().isNotFound())
            .andExpect(view().name("product"))
            .andExpect(model().attributeExists("errorMessage"));
    }
}
