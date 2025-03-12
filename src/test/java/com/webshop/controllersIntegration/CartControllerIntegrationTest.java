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

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql("/test-data-full.sql")
class CartControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @SneakyThrows
    void testGetCart() {
        mockMvc.perform(MockMvcRequestBuilders.get("/cart"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(view().name("cart"))
            .andExpect(model().attributeExists("cart"))
            .andExpect(model().attributeExists("totalPrice"));
    }

    @Test
    @SneakyThrows
    void testAddCartItem() {
        mockMvc.perform(MockMvcRequestBuilders.post("/cart/add")
                .param("productId", "9")
                .param("quantity", "1"))
            .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
            .andExpect(redirectedUrlPattern("**"));
    }

    @Test
    @SneakyThrows
    void testUpdateCartItem()  {
        mockMvc.perform(MockMvcRequestBuilders.post("/cart/update")
                .param("productId", "6")
                .param("quantity", "5"))
            .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
            .andExpect(redirectedUrlPattern("**"));
    }

    @Test
    @SneakyThrows
    void testRemoveCartItem() {
        mockMvc.perform(MockMvcRequestBuilders.post("/cart/remove")
                .param("productId", "7"))
            .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
            .andExpect(redirectedUrlPattern("**"));
    }

    @Test
    @SneakyThrows
    void testCompleteOrder()  {
        mockMvc.perform(MockMvcRequestBuilders.post("/cart/checkout"))
            .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
            .andExpect(redirectedUrlPattern("/orders/*"));
    }

    @Test
    @SneakyThrows
    void testHandleCartIsEmptyException() {

        mockMvc.perform(MockMvcRequestBuilders.post("/cart/remove")
            .param("productId", "6"));
        mockMvc.perform(MockMvcRequestBuilders.post("/cart/remove")
            .param("productId", "7"));

        mockMvc.perform(MockMvcRequestBuilders.post("/cart/checkout"))
            .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
            .andExpect(redirectedUrl("/cart"))
            .andExpect(flash().attributeExists("errorMessage"));
    }
}