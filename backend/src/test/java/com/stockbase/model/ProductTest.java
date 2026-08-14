package com.stockbase.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Boundary tests for the stock-status domain logic that drives reorder alerts
 * and dashboard badges. Low stock is strictly positive quantity at or below the
 * reorder threshold; zero is out of stock, not low.
 */
class ProductTest {

    private Product withQuantity(int quantity, int threshold) {
        return Product.builder().quantity(quantity).reorderThreshold(threshold).build();
    }

    @Test
    @DisplayName("Quantity at the reorder threshold counts as low stock")
    void atThreshold_isLowStock() {
        Product p = withQuantity(10, 10);
        assertThat(p.isLowStock()).isTrue();
        assertThat(p.isOutOfStock()).isFalse();
    }

    @Test
    @DisplayName("Quantity above the threshold is neither low nor out of stock")
    void aboveThreshold_isHealthy() {
        Product p = withQuantity(11, 10);
        assertThat(p.isLowStock()).isFalse();
        assertThat(p.isOutOfStock()).isFalse();
    }

    @Test
    @DisplayName("Zero quantity is out of stock, and not reported as low stock")
    void zeroQuantity_isOutOfStockNotLow() {
        Product p = withQuantity(0, 10);
        assertThat(p.isOutOfStock()).isTrue();
        assertThat(p.isLowStock()).isFalse();
    }
}
