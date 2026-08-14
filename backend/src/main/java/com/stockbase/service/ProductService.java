package com.stockbase.service;

import com.stockbase.exception.DuplicateResourceException;
import com.stockbase.exception.ResourceNotFoundException;
import com.stockbase.model.Category;
import com.stockbase.model.Product;
import com.stockbase.model.Supplier;
import com.stockbase.repository.CategoryRepository;
import com.stockbase.repository.ProductRepository;
import com.stockbase.repository.SupplierRepository;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /** Paginated + sortable product listing for large catalogs (e.g. ?page=0&size=20&sort=name). */
    public Page<Product> getPaged(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    public Product getById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
    }

    public List<Product> getLowStockProducts() {
        return productRepository.findLowStockProducts();
    }

    public List<Product> search(String q) {
        return productRepository.search(q);
    }

    @Transactional
    public Product create(ProductRequest req) {
        if (productRepository.existsBySku(req.getSku().toUpperCase())) {
            throw new DuplicateResourceException("SKU already exists: " + req.getSku());
        }
        return productRepository.save(buildProduct(null, req));
    }

    @Transactional
    public Product update(Long id, ProductRequest req) {
        Product existing = getById(id);
        if (!existing.getSku().equalsIgnoreCase(req.getSku())
                && productRepository.existsBySku(req.getSku().toUpperCase())) {
            throw new DuplicateResourceException("SKU already exists: " + req.getSku());
        }
        return productRepository.save(buildProduct(existing, req));
    }

    @Transactional
    public void delete(Long id) {
        productRepository.deleteById(id);
    }

    private Product buildProduct(Product existing, ProductRequest req) {
        Product p = existing != null ? existing : new Product();
        p.setName(req.getName().trim());
        p.setSku(req.getSku().trim().toUpperCase());
        p.setDescription(req.getDescription());
        p.setPrice(req.getPrice());
        p.setQuantity(req.getQuantity());
        p.setReorderThreshold(req.getReorderThreshold() != null ? req.getReorderThreshold() : 10);

        if (req.getCategoryId() != null) {
            Category cat = categoryRepository.findById(req.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + req.getCategoryId()));
            p.setCategory(cat);
        } else {
            p.setCategory(null);
        }

        if (req.getSupplierId() != null) {
            Supplier sup = supplierRepository.findById(req.getSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supplier not found: " + req.getSupplierId()));
            p.setSupplier(sup);
        } else {
            p.setSupplier(null);
        }

        return p;
    }

    @Data
    public static class ProductRequest {
        @NotBlank
        private String name;
        @NotBlank
        private String sku;
        private String description;
        @NotNull
        @Min(0)
        private BigDecimal price;
        @Min(0)
        private int quantity;
        private Integer reorderThreshold;
        private Long categoryId;
        private Long supplierId;
    }
}
