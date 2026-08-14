package com.stockbase.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end security tests that exercise the real Spring Security filter chain
 * and method-level @PreAuthorize rules against an in-memory H2 database. These
 * prove the role-based access control actually enforces the contract: reads are
 * open to any authenticated user, writes are admin-only, and anonymous callers
 * are refused.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ProductSecurityIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private String newProductJson(String sku) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "name", "Test Product",
                "sku", sku,
                "price", 12.50,
                "quantity", 5
        ));
    }

    @Test
    @DisplayName("Any authenticated user can read the product list")
    @WithMockUser(roles = "USER")
    void authenticatedUser_canReadProducts() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("A non-admin user is forbidden from creating a product (403)")
    @WithMockUser(roles = "USER")
    void standardUser_cannotCreateProduct() throws Exception {
        mockMvc.perform(post("/api/products")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newProductJson("SEC-USER-001")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("An admin user can create a product (201)")
    @WithMockUser(roles = "ADMIN")
    void adminUser_canCreateProduct() throws Exception {
        mockMvc.perform(post("/api/products")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newProductJson("SEC-ADMIN-001")))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("An anonymous caller cannot read protected endpoints")
    @WithAnonymousUser
    void anonymous_isRejected() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("The actuator health endpoint is public")
    @WithAnonymousUser
    void health_isPublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }
}
