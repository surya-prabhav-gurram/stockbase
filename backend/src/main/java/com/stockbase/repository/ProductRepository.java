package com.stockbase.repository;

import com.stockbase.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsBySku(String sku);

    @Query("SELECT p FROM Product p WHERE p.quantity <= p.reorderThreshold ORDER BY p.quantity ASC")
    List<Product> findLowStockProducts();

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<Product> search(@Param("q") String q);

    @Query("SELECT c.name, SUM(p.price * p.quantity) FROM Product p JOIN p.category c GROUP BY c.name ORDER BY SUM(p.price * p.quantity) DESC")
    List<Object[]> getInventoryValueByCategory();

    @Query("SELECT s.name, COUNT(p), SUM(p.price * p.quantity) FROM Product p JOIN p.supplier s GROUP BY s.name ORDER BY SUM(p.price * p.quantity) DESC")
    List<Object[]> getInventoryBySupplier();
}
