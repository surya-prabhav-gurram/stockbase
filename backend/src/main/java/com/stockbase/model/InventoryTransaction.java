package com.stockbase.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "inventory_transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryTransaction {

    public enum Type { STOCK_IN, STOCK_OUT, ADJUSTMENT }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    @Column(nullable = false)
    private int quantity;

    private int quantityBefore;
    private int quantityAfter;

    private String reason;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "performed_by")
    private User performedBy;

    @CreationTimestamp
    private Instant createdAt;
}
