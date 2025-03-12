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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql("/test-data-full.sql")
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @SneakyThrows
    void testGetCompletedOrder() {
        mockMvc.perform(MockMvcRequestBuilders.get("/orders/7"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(view().name("order"))
            .andExpect(model().attributeExists("order"));
    }

    @Test
    @SneakyThrows
    void testGetAllCompletedOrders() {
        mockMvc.perform(MockMvcRequestBuilders.get("/orders"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(view().name("orders"))
            .andExpect(model().attributeExists("orders"))
            .andExpect(model().attributeExists("totalPrice"));
    }

    @Test
    @SneakyThrows
    void testHandleCustomerOrderNotFoundException() {
        mockMvc.perform(MockMvcRequestBuilders.get("/orders/999"))
            .andExpect(MockMvcResultMatchers.status().isNotFound())
            .andExpect(view().name("order"))
            .andExpect(model().attributeExists("errorMessage"));
    }
}
