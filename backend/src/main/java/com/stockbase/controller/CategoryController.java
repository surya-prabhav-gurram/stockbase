package com.stockbase.controller;

import com.stockbase.exception.DuplicateResourceException;
import com.stockbase.exception.ResourceNotFoundException;
import com.stockbase.model.Category;
import com.stockbase.repository.CategoryRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;

    @GetMapping
    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    @GetMapping("/{id}")
    public Category getById(@PathVariable Long id) {
        return categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Category> create(@Valid @RequestBody CategoryRequest req) {
        if (categoryRepository.existsByName(req.name)) {
            throw new DuplicateResourceException("Category already exists: " + req.name);
        }
        Category cat = Category.builder().name(req.name).description(req.description).build();
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryRepository.save(cat));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Category update(@PathVariable Long id, @Valid @RequestBody CategoryRequest req) {
        Category cat = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
        cat.setName(req.name);
        cat.setDescription(req.description);
        return categoryRepository.save(cat);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Data static class CategoryRequest {
        @NotBlank String name;
        String description;
    }
}
