package com.stockbase.service;

import com.stockbase.model.Product;
import com.stockbase.repository.ProductRepository;
import com.stockbase.repository.TransactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit tests for dashboard aggregation: total inventory value and the
 * in-stock / low-stock / out-of-stock classification counts.
 */
@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private TransactionRepository transactionRepository;

    @InjectMocks private ReportService reportService;

    private Product product(String sku, String price, int qty, int threshold) {
        return Product.builder().sku(sku).price(new BigDecimal(price))
                .quantity(qty).reorderThreshold(threshold).build();
    }

    @Test
    @DisplayName("Dashboard stats classify stock levels and sum inventory value")
    void dashboardStats_areComputedCorrectly() {
        List<Product> products = List.of(
                product("A", "10.00", 100, 10),  // in stock
                product("B", "5.00", 8, 10),      // low stock (0 < 8 <= 10)
                product("C", "20.00", 0, 10),     // out of stock
                product("D", "2.50", 40, 10)      // in stock
        );
        when(productRepository.findAll()).thenReturn(products);
        when(transactionRepository.count()).thenReturn(12L);

        Map<String, Object> stats = reportService.getDashboardStats();

        assertThat(stats.get("totalProducts")).isEqualTo(4L);
        assertThat(stats.get("lowStockCount")).isEqualTo(1L);
        assertThat(stats.get("outOfStockCount")).isEqualTo(1L);
        // Value = 100*10 + 8*5 + 0*20 + 40*2.50 = 1000 + 40 + 0 + 100 = 1140.00
        assertThat(stats.get("totalInventoryValue")).isEqualTo(new BigDecimal("1140.00"));
        assertThat(stats.get("totalTransactions")).isEqualTo(12L);
    }

    @Test
    @DisplayName("CSV export includes a header and one row per product")
    void csvExport_hasHeaderAndRows() throws Exception {
        when(productRepository.findAll()).thenReturn(List.of(
                product("OFF-001", "4.99", 250, 50),
                product("ELE-001", "29.99", 18, 20)
        ));

        String csv = reportService.exportProductsCsv();

        assertThat(csv).contains("SKU").contains("Status");
        assertThat(csv).contains("OFF-001").contains("ELE-001");
        assertThat(csv.strip().lines().count()).isEqualTo(3); // header + 2 rows
    }
}
