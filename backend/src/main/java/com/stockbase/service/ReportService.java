package com.stockbase.service;

import com.stockbase.model.Product;
import com.stockbase.repository.ProductRepository;
import com.stockbase.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ProductRepository productRepository;
    private final TransactionRepository transactionRepository;

    public Map<String, Object> getDashboardStats() {
        List<Product> all = productRepository.findAll();
        long total = all.size();
        long lowStock = all.stream().filter(Product::isLowStock).count();
        long outOfStock = all.stream().filter(Product::isOutOfStock).count();
        long inStock = all.stream().filter(p -> !p.isLowStock()).count();
        BigDecimal totalValue = all.stream()
            .map(p -> p.getPrice().multiply(BigDecimal.valueOf(p.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalProducts", total);
        stats.put("inStockCount", inStock);
        stats.put("lowStockCount", lowStock);
        stats.put("outOfStockCount", outOfStock);
        stats.put("totalInventoryValue", totalValue);
        stats.put("totalTransactions", transactionRepository.count());
        return stats;
    }

    public List<Map<String, Object>> getInventoryValueByCategory() {
        List<Object[]> rows = productRepository.getInventoryValueByCategory();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("categoryName", row[0]);
            m.put("totalValue", row[1]);
            result.add(m);
        }
        return result;
    }

    public List<Map<String, Object>> getInventoryBySupplier() {
        List<Object[]> rows = productRepository.getInventoryBySupplier();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("supplierName", row[0]);
            m.put("productCount", row[1]);
            m.put("totalValue", row[2]);
            result.add(m);
        }
        return result;
    }

    public List<Product> getLowStockReport() {
        return productRepository.findLowStockProducts();
    }

    public String exportProductsCsv() throws IOException {
        List<Product> products = productRepository.findAll();
        StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT
                .withHeader("ID","Name","SKU","Category","Supplier","Price","Quantity","Reorder Threshold","Status"))) {
            for (Product p : products) {
                String status = p.isOutOfStock() ? "Out of Stock" : p.isLowStock() ? "Low Stock" : "In Stock";
                printer.printRecord(
                    p.getId(), p.getName(), p.getSku(),
                    p.getCategory() != null ? p.getCategory().getName() : "",
                    p.getSupplier() != null ? p.getSupplier().getName() : "",
                    p.getPrice(), p.getQuantity(), p.getReorderThreshold(), status
                );
            }
        }
        return sw.toString();
    }

    public String exportLowStockCsv() throws IOException {
        List<Product> products = productRepository.findLowStockProducts();
        StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT
                .withHeader("ID","Name","SKU","Supplier","Quantity","Reorder Threshold","Shortage"))) {
            for (Product p : products) {
                int shortage = Math.max(0, p.getReorderThreshold() - p.getQuantity());
                printer.printRecord(
                    p.getId(), p.getName(), p.getSku(),
                    p.getSupplier() != null ? p.getSupplier().getName() : "",
                    p.getQuantity(), p.getReorderThreshold(), shortage
                );
            }
        }
        return sw.toString();
    }
}
