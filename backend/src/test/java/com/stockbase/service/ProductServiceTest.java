package com.stockbase.service;

import com.stockbase.exception.DuplicateResourceException;
import com.stockbase.exception.ResourceNotFoundException;
import com.stockbase.model.Category;
import com.stockbase.model.Product;
import com.stockbase.repository.CategoryRepository;
import com.stockbase.repository.ProductRepository;
import com.stockbase.repository.SupplierRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for product creation/update rules: SKU uniqueness, SKU
 * normalisation (trim + upper-case), and the not-found paths for missing
 * products and referenced entities.
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private SupplierRepository supplierRepository;

    @InjectMocks private ProductService productService;

    private ProductService.ProductRequest baseRequest(String sku) {
        ProductService.ProductRequest req = new ProductService.ProductRequest();
        req.setName("  Wireless Mouse  ");
        req.setSku(sku);
        req.setPrice(new BigDecimal("29.99"));
        req.setQuantity(10);
        return req;
    }

    @Test
    @DisplayName("create() upper-cases the SKU, trims the name, and persists")
    void create_normalisesSkuAndSaves() {
        when(productRepository.existsBySku("ELE-001")).thenReturn(false);
        when(productRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Product saved = productService.create(baseRequest("ele-001"));

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        assertThat(captor.getValue().getSku()).isEqualTo("ELE-001");
        assertThat(captor.getValue().getName()).isEqualTo("Wireless Mouse"); // trimmed
        assertThat(saved.getReorderThreshold()).isEqualTo(10); // default applied
    }

    @Test
    @DisplayName("create() rejects a duplicate SKU without saving")
    void create_duplicateSku_isRejected() {
        when(productRepository.existsBySku("ELE-001")).thenReturn(true);

        assertThatThrownBy(() -> productService.create(baseRequest("ele-001")))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("SKU already exists");
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("getById() raises ResourceNotFoundException for a missing id")
    void getById_missing_notFound() {
        when(productRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getById(42L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("update() allows keeping the same SKU on the same product")
    void update_sameSku_isAllowed() {
        Product existing = Product.builder().id(1L).name("Old").sku("ELE-001")
                .price(new BigDecimal("1.00")).quantity(5).reorderThreshold(10).build();
        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Product result = productService.update(1L, baseRequest("ELE-001"));

        assertThat(result.getName()).isEqualTo("Wireless Mouse");
        // existsBySku must NOT be consulted when the SKU is unchanged.
        verify(productRepository, never()).existsBySku(any());
    }

    @Test
    @DisplayName("update() rejects changing to a SKU owned by another product")
    void update_toExistingSku_isRejected() {
        Product existing = Product.builder().id(1L).name("Old").sku("OLD-001")
                .price(new BigDecimal("1.00")).quantity(5).reorderThreshold(10).build();
        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.existsBySku("ELE-001")).thenReturn(true);

        assertThatThrownBy(() -> productService.update(1L, baseRequest("ELE-001")))
                .isInstanceOf(DuplicateResourceException.class);
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("create() with an unknown category id raises ResourceNotFoundException")
    void create_unknownCategory_notFound() {
        ProductService.ProductRequest req = baseRequest("NEW-001");
        req.setCategoryId(7L);
        when(productRepository.existsBySku("NEW-001")).thenReturn(false);
        when(categoryRepository.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.create(req))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category not found");
    }

    @Test
    @DisplayName("create() resolves a valid category reference onto the product")
    void create_withValidCategory_setsCategory() {
        ProductService.ProductRequest req = baseRequest("NEW-002");
        req.setCategoryId(3L);
        Category cat = Category.builder().id(3L).name("Electronics").build();
        when(productRepository.existsBySku("NEW-002")).thenReturn(false);
        when(categoryRepository.findById(3L)).thenReturn(Optional.of(cat));
        when(productRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Product saved = productService.create(req);

        assertThat(saved.getCategory()).isEqualTo(cat);
    }

    @Test
    @DisplayName("delete() delegates to the repository")
    void delete_delegatesToRepository() {
        productService.delete(5L);
        verify(productRepository).deleteById(5L);
    }
}
