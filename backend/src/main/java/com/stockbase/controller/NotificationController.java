package com.stockbase.controller;

import com.stockbase.model.Product;
import com.stockbase.notification.LowStockNotifier;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "External low-stock alerting")
public class NotificationController {

    private final LowStockNotifier lowStockNotifier;

    @PostMapping("/low-stock/run")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Run the low-stock check now and push alerts to the external webhook")
    public Map<String, Object> runLowStockCheck() {
        List<Product> notified = lowStockNotifier.checkAndNotify();
        return Map.of(
                "notifiedCount", notified.size(),
                "products", notified.stream()
                        .map(p -> Map.<String, Object>of(
                                "id", p.getId(),
                                "name", p.getName(),
                                "sku", p.getSku(),
                                "quantity", p.getQuantity()))
                        .toList());
    }
}
