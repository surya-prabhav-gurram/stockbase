package com.stockbase.controller;

import com.stockbase.model.Product;
import com.stockbase.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/dashboard")
    public Map<String, Object> getDashboard() {
        return reportService.getDashboardStats();
    }

    @GetMapping("/inventory-by-category")
    public List<Map<String, Object>> getByCategory() {
        return reportService.getInventoryValueByCategory();
    }

    @GetMapping("/inventory-by-supplier")
    public List<Map<String, Object>> getBySupplier() {
        return reportService.getInventoryBySupplier();
    }

    @GetMapping("/low-stock")
    public List<Product> getLowStock() {
        return reportService.getLowStockReport();
    }

    @GetMapping("/export/products.csv")
    public ResponseEntity<String> exportProducts() throws IOException {
        String csv = reportService.exportProductsCsv();
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"products.csv\"")
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(csv);
    }

    @GetMapping("/export/low-stock.csv")
    public ResponseEntity<String> exportLowStock() throws IOException {
        String csv = reportService.exportLowStockCsv();
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"low-stock.csv\"")
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(csv);
    }
}
