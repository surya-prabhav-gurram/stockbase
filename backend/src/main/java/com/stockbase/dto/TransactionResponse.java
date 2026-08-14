package com.stockbase.dto;

import com.stockbase.model.InventoryTransaction;

import java.time.Instant;

/**
 * API response model for an inventory transaction.
 *
 * <p>The controllers previously returned the {@link InventoryTransaction} entity
 * directly, which dragged along the eagerly-loaded {@code performedBy} User —
 * including its (hashed) password — into the JSON. This DTO decouples the wire
 * format from the persistence model and exposes only the fields a client needs,
 * closing that information-disclosure hole by construction.
 */
public record TransactionResponse(
        Long id,
        String type,
        int quantity,
        int quantityBefore,
        int quantityAfter,
        String reason,
        String notes,
        Instant createdAt,
        ProductSummary product,
        UserSummary performedBy
) {

    public record ProductSummary(Long id, String name, String sku) {}

    public record UserSummary(Long id, String fullName) {}

    public static TransactionResponse from(InventoryTransaction t) {
        return new TransactionResponse(
                t.getId(),
                t.getType() == null ? null : t.getType().name(),
                t.getQuantity(),
                t.getQuantityBefore(),
                t.getQuantityAfter(),
                t.getReason(),
                t.getNotes(),
                t.getCreatedAt(),
                t.getProduct() == null ? null
                        : new ProductSummary(t.getProduct().getId(), t.getProduct().getName(), t.getProduct().getSku()),
                t.getPerformedBy() == null ? null
                        : new UserSummary(t.getPerformedBy().getId(), t.getPerformedBy().getFullName())
        );
    }
}
