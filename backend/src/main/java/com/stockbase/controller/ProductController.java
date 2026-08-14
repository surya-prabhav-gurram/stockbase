package com.stockbase.controller;

import com.stockbase.model.Product;
import com.stockbase.service.ProductService;
import com.stockbase.service.ProductService.ProductRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product catalog and inventory levels")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "List all products")
    public List<Product> getAll() {
        return productService.getAllProducts();
    }

    @GetMapping("/page")
    @Operation(summary = "List products with pagination and sorting")
    public Page<Product> getPage(@ParameterObject Pageable pageable) {
        return productService.getPaged(pageable);
    }

    @GetMapping("/{id}")
    public Product getById(@PathVariable Long id) {
        return productService.getById(id);
    }

    @GetMapping("/low-stock")
    public List<Product> getLowStock() {
        return productService.getLowStockProducts();
    }

    @GetMapping("/search")
    public List<Product> search(@RequestParam String q) {
        return productService.search(q);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> create(@Valid @RequestBody ProductRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Product update(@PathVariable Long id, @Valid @RequestBody ProductRequest req) {
        return productService.update(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
