package com.stockbase.notification;

import com.stockbase.model.Product;
import com.stockbase.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the low-stock notification logic: which products get alerted,
 * de-duplication across runs, and re-alerting after recovery. Uses a mocked
 * NotificationSender so no HTTP/webhook is involved.
 */
@ExtendWith(MockitoExtension.class)
class LowStockNotifierTest {

    @Mock private ProductRepository productRepository;
    @Mock private NotificationSender sender;

    @InjectMocks private LowStockNotifier notifier;

    private Product product(long id, String sku, int qty, int threshold) {
        return Product.builder().id(id).name("Product " + id).sku(sku)
                .price(new BigDecimal("1.00")).quantity(qty).reorderThreshold(threshold).build();
    }

    @Test
    @DisplayName("Notifies once per newly low product, and not again while it stays low")
    void notifiesOncePerNewlyLowProduct() {
        when(productRepository.findLowStockProducts())
                .thenReturn(List.of(product(1, "A", 2, 10), product(2, "B", 0, 5)));

        List<Product> first = notifier.checkAndNotify();
        assertThat(first).hasSize(2);
        verify(sender, times(2)).send(anyString(), anyString());

        // Same products still low on the next run → no duplicate alerts.
        List<Product> second = notifier.checkAndNotify();
        assertThat(second).isEmpty();
        verifyNoMoreInteractions(sender);
    }

    @Test
    @DisplayName("Re-alerts a product that recovered above threshold and then dropped again")
    void reAlertsAfterRecovery() {
        when(productRepository.findLowStockProducts())
                .thenReturn(List.of(product(1, "A", 2, 10)))  // low
                .thenReturn(List.of())                          // recovered
                .thenReturn(List.of(product(1, "A", 1, 10)));   // low again

        assertThat(notifier.checkAndNotify()).hasSize(1); // alert
        assertThat(notifier.checkAndNotify()).isEmpty();  // recovered, forgotten
        assertThat(notifier.checkAndNotify()).hasSize(1); // dropped again → alert again

        verify(sender, times(2)).send(anyString(), anyString());
    }

    @Test
    @DisplayName("No products low → no notifications sent")
    void noLowStock_noNotifications() {
        when(productRepository.findLowStockProducts()).thenReturn(List.of());
        assertThat(notifier.checkAndNotify()).isEmpty();
        verifyNoInteractions(sender);
    }
}
