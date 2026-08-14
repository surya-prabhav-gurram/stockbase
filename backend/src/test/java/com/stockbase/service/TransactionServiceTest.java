package com.stockbase.service;

import com.stockbase.exception.BadRequestException;
import com.stockbase.exception.ResourceNotFoundException;
import com.stockbase.model.InventoryTransaction;
import com.stockbase.model.InventoryTransaction.Type;
import com.stockbase.model.Product;
import com.stockbase.model.User;
import com.stockbase.repository.ProductRepository;
import com.stockbase.repository.TransactionRepository;
import com.stockbase.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the core inventory business logic: how a stock movement
 * updates a product's quantity, when a withdrawal is rejected, and what the
 * resulting audit record looks like. Pure Mockito — no Spring context, no DB —
 * so these run in milliseconds and are the fastest signal that the domain rules
 * are correct.
 */
@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private TransactionService transactionService;

    private Product product;

    @BeforeEach
    void setUp() {
        // record() reads the current user from the SecurityContext for the audit trail.
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin@stockbase.com", null));

        product = Product.builder().id(1L).name("Wireless Mouse").sku("ELE-001").quantity(20).build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private TransactionService.TransactionRequest request(Type type, int qty) {
        TransactionService.TransactionRequest req = new TransactionService.TransactionRequest();
        req.setProductId(1L);
        req.setType(type);
        req.setQuantity(qty);
        return req;
    }

    @Test
    @DisplayName("STOCK_IN increases quantity and records before/after on the audit row")
    void stockIn_increasesQuantity() {
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));
        when(userRepository.findByEmail("admin@stockbase.com"))
                .thenReturn(Optional.of(User.builder().id(1L).email("admin@stockbase.com").build()));
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        InventoryTransaction tx = transactionService.record(request(Type.STOCK_IN, 5));

        assertThat(product.getQuantity()).isEqualTo(25);
        assertThat(tx.getQuantityBefore()).isEqualTo(20);
        assertThat(tx.getQuantityAfter()).isEqualTo(25);
        assertThat(tx.getType()).isEqualTo(Type.STOCK_IN);
        assertThat(tx.getPerformedBy()).isNotNull();
        verify(productRepository).save(product);
    }

    @Test
    @DisplayName("STOCK_OUT with sufficient stock decreases quantity")
    void stockOut_sufficientStock_decreasesQuantity() {
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        InventoryTransaction tx = transactionService.record(request(Type.STOCK_OUT, 8));

        assertThat(product.getQuantity()).isEqualTo(12);
        assertThat(tx.getQuantityAfter()).isEqualTo(12);
    }

    @Test
    @DisplayName("STOCK_OUT beyond available stock is rejected and nothing is persisted")
    void stockOut_insufficientStock_isRejected() {
        product.setQuantity(3);
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> transactionService.record(request(Type.STOCK_OUT, 5)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Insufficient stock");

        // The guard must fire BEFORE any state change — no save on either repository.
        assertThat(product.getQuantity()).isEqualTo(3);
        verify(productRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("ADJUSTMENT sets quantity to the absolute target value")
    void adjustment_setsAbsoluteQuantity() {
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        InventoryTransaction tx = transactionService.record(request(Type.ADJUSTMENT, 7));

        assertThat(product.getQuantity()).isEqualTo(7);
        assertThat(tx.getQuantityBefore()).isEqualTo(20);
        assertThat(tx.getQuantityAfter()).isEqualTo(7);
    }

    @Test
    @DisplayName("Recording against a missing product raises ResourceNotFoundException")
    void record_unknownProduct_notFound() {
        when(productRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());
        TransactionService.TransactionRequest req = request(Type.STOCK_IN, 1);
        req.setProductId(99L);

        assertThatThrownBy(() -> transactionService.record(req))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Audit row captures the exact quantities via the persisted entity")
    void record_persistsAuditRowWithCapturedQuantities() {
        product.setQuantity(50);
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        transactionService.record(request(Type.STOCK_OUT, 20));

        ArgumentCaptor<InventoryTransaction> captor = ArgumentCaptor.forClass(InventoryTransaction.class);
        verify(transactionRepository).save(captor.capture());
        InventoryTransaction saved = captor.getValue();
        assertThat(saved.getQuantityBefore()).isEqualTo(50);
        assertThat(saved.getQuantityAfter()).isEqualTo(30);
        assertThat(saved.getQuantity()).isEqualTo(20);
    }
}
