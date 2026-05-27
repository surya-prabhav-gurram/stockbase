package com.stockbase.service;

import com.stockbase.exception.BadRequestException;
import com.stockbase.exception.ResourceNotFoundException;
import com.stockbase.model.InventoryTransaction;
import com.stockbase.model.Product;
import com.stockbase.model.User;
import com.stockbase.repository.ProductRepository;
import com.stockbase.repository.TransactionRepository;
import com.stockbase.repository.UserRepository;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public List<InventoryTransaction> getAll() {
        return transactionRepository.findAll()
                .stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .toList();
    }

    public List<InventoryTransaction> getRecent(int limit) {
        return transactionRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, limit));
    }

    public List<InventoryTransaction> getByProduct(Long productId) {
        return transactionRepository.findByProductIdOrderByCreatedAtDesc(productId);
    }

    @Transactional
    public InventoryTransaction record(TransactionRequest req) {
        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + req.getProductId()));

        int before = product.getQuantity();
        int after;

        switch (req.getType()) {
            case STOCK_IN -> after = before + req.getQuantity();
            case STOCK_OUT -> {
                if (before < req.getQuantity()) {
                    throw new BadRequestException(
                            "Insufficient stock. Available: " + before + ", requested: " + req.getQuantity());
                }
                after = before - req.getQuantity();
            }
            case ADJUSTMENT -> after = req.getQuantity();
            default -> throw new BadRequestException("Unknown transaction type");
        }

        product.setQuantity(after);
        productRepository.save(product);

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User performer = userRepository.findByEmail(email).orElse(null);

        InventoryTransaction tx = InventoryTransaction.builder()
                .product(product)
                .type(req.getType())
                .quantity(req.getQuantity())
                .quantityBefore(before)
                .quantityAfter(after)
                .reason(req.getReason())
                .notes(req.getNotes())
                .performedBy(performer)
                .build();

        return transactionRepository.save(tx);
    }

    @Data
    public static class TransactionRequest {
        @NotNull
        private Long productId;
        @NotNull
        private InventoryTransaction.Type type;
        @NotNull
        @Min(1)
        private Integer quantity;
        private String reason;
        private String notes;
    }
}
