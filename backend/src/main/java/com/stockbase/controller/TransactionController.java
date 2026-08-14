package com.stockbase.controller;

import com.stockbase.dto.TransactionResponse;
import com.stockbase.service.TransactionService;
import com.stockbase.service.TransactionService.TransactionRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Inventory stock movements and audit history")
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    @Operation(summary = "List all transactions, newest first")
    public List<TransactionResponse> getAll() {
        return transactionService.getAll().stream().map(TransactionResponse::from).toList();
    }

    @GetMapping("/recent")
    @Operation(summary = "List the most recent transactions")
    public List<TransactionResponse> getRecent(@RequestParam(defaultValue = "20") int limit) {
        return transactionService.getRecent(limit).stream().map(TransactionResponse::from).toList();
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "List transactions for a single product")
    public List<TransactionResponse> getByProduct(@PathVariable Long productId) {
        return transactionService.getByProduct(productId).stream().map(TransactionResponse::from).toList();
    }

    @PostMapping
    @Operation(summary = "Record a stock movement (in / out / adjustment)")
    public ResponseEntity<TransactionResponse> record(@Valid @RequestBody TransactionRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(TransactionResponse.from(transactionService.record(req)));
    }
}
