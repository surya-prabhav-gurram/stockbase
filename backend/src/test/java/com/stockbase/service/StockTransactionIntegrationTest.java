package com.stockbase.service;

import com.stockbase.exception.BadRequestException;
import com.stockbase.model.InventoryTransaction.Type;
import com.stockbase.model.Product;
import com.stockbase.repository.ProductRepository;
import com.stockbase.service.TransactionService.TransactionRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * End-to-end test over a real (H2) database that drives the pessimistic-lock
 * fetch path in {@link TransactionService#record}. It verifies the invariant the
 * locking is there to protect: stock is persisted correctly on a valid movement
 * and can never be driven below zero by an over-sized withdrawal.
 */
@SpringBootTest
@Transactional // each test rolls back, so the seeded product/SKU doesn't leak between methods
class StockTransactionIntegrationTest {

    @Autowired private TransactionService transactionService;
    @Autowired private ProductRepository productRepository;

    private Long productId;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin@stockbase.com", null));
        Product p = productRepository.save(Product.builder()
                .name("Integration Widget").sku("ITEST-001")
                .price(new BigDecimal("9.99")).quantity(5).reorderThreshold(2).build());
        productId = p.getId();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private TransactionRequest req(Type type, int qty) {
        TransactionRequest r = new TransactionRequest();
        r.setProductId(productId);
        r.setType(type);
        r.setQuantity(qty);
        return r;
    }

    @Test
    @DisplayName("A valid stock-out persists the new quantity through the locked path")
    void stockOut_persistsNewQuantity() {
        transactionService.record(req(Type.STOCK_OUT, 4));
        assertThat(productRepository.findById(productId).orElseThrow().getQuantity()).isEqualTo(1);
    }

    @Test
    @DisplayName("Withdrawing more than available is rejected and leaves stock unchanged")
    void stockOut_overAvailable_isRejectedAndStockUnchanged() {
        transactionService.record(req(Type.STOCK_OUT, 4)); // 5 -> 1
        assertThatThrownBy(() -> transactionService.record(req(Type.STOCK_OUT, 4)))
                .isInstanceOf(BadRequestException.class);
        assertThat(productRepository.findById(productId).orElseThrow().getQuantity()).isEqualTo(1);
    }
}
