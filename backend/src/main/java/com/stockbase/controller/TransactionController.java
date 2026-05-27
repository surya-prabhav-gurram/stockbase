package com.stockbase.controller;

import com.stockbase.model.InventoryTransaction;
import com.stockbase.service.TransactionService;
import com.stockbase.service.TransactionService.TransactionRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    public List<InventoryTransaction> getAll() {
        return transactionService.getAll();
    }

    @GetMapping("/recent")
    public List<InventoryTransaction> getRecent(@RequestParam(defaultValue = "20") int limit) {
        return transactionService.getRecent(limit);
    }

    @GetMapping("/product/{productId}")
    public List<InventoryTransaction> getByProduct(@PathVariable Long productId) {
        return transactionService.getByProduct(productId);
    }

    @PostMapping
    public ResponseEntity<InventoryTransaction> record(@Valid @RequestBody TransactionRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.record(req));
    }
}
