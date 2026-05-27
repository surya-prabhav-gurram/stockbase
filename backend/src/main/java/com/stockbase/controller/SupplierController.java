package com.stockbase.controller;

import com.stockbase.exception.ResourceNotFoundException;
import com.stockbase.model.Supplier;
import com.stockbase.repository.SupplierRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierRepository supplierRepository;

    @GetMapping
    public List<Supplier> getAll() { return supplierRepository.findAll(); }

    @GetMapping("/{id}")
    public Supplier getById(@PathVariable Long id) {
        return supplierRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Supplier not found: " + id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Supplier> create(@Valid @RequestBody SupplierRequest req) {
        Supplier s = Supplier.builder()
            .name(req.name).contactEmail(req.contactEmail)
            .phone(req.phone).address(req.address).notes(req.notes)
            .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(supplierRepository.save(s));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Supplier update(@PathVariable Long id, @Valid @RequestBody SupplierRequest req) {
        Supplier s = supplierRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Supplier not found: " + id));
        s.setName(req.name); s.setContactEmail(req.contactEmail);
        s.setPhone(req.phone); s.setAddress(req.address); s.setNotes(req.notes);
        return supplierRepository.save(s);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        supplierRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Data static class SupplierRequest {
        @NotBlank String name;
        @Email String contactEmail;
        String phone, address, notes;
    }
}
