package com.stockbase.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockbase.dto.TransactionResponse;
import com.stockbase.model.InventoryTransaction;
import com.stockbase.model.InventoryTransaction.Type;
import com.stockbase.model.Product;
import com.stockbase.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regression tests for the information-disclosure fix: a user's (hashed) password
 * must never appear in serialized API output — neither directly on a User nor
 * nested inside a transaction's performedBy.
 */
class SerializationSecurityTest {

    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

    @Test
    @DisplayName("Serialising a User never emits the password field")
    void userJson_hasNoPassword() throws Exception {
        User user = User.builder()
                .id(1L).fullName("Admin User").email("admin@stockbase.com")
                .password("$2a$10$examplebcrypthashvalue")
                .role(User.Role.ADMIN)
                .build();

        String json = mapper.writeValueAsString(user);

        assertThat(json).doesNotContain("password");
        assertThat(json).doesNotContain("$2a$");
        assertThat(json).contains("admin@stockbase.com"); // non-sensitive fields still present
    }

    @Test
    @DisplayName("TransactionResponse exposes performer name but not the password")
    void transactionResponseJson_hasNoNestedPassword() throws Exception {
        User performer = User.builder()
                .id(1L).fullName("Admin User").email("admin@stockbase.com")
                .password("$2a$10$examplebcrypthashvalue").role(User.Role.ADMIN).build();
        Product product = Product.builder().id(5L).name("Wireless Mouse").sku("ELE-001").build();
        InventoryTransaction tx = InventoryTransaction.builder()
                .id(100L).product(product).type(Type.STOCK_OUT)
                .quantity(4).quantityBefore(20).quantityAfter(16)
                .performedBy(performer).build();

        String json = mapper.writeValueAsString(TransactionResponse.from(tx));

        assertThat(json).doesNotContain("password");
        assertThat(json).doesNotContain("$2a$");
        assertThat(json).contains("Admin User"); // performer name still exposed for the UI
        assertThat(json).contains("ELE-001");
    }
}
