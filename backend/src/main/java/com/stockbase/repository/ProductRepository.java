package com.stockbase.repository;

import com.stockbase.model.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsBySku(String sku);

    /**
     * Fetch a product while holding a row-level write lock (SELECT ... FOR UPDATE)
     * for the duration of the surrounding transaction. Stock movements use this so
     * two concurrent stock-outs on the same product are serialised and cannot both
     * pass the "enough stock?" check and oversell below zero.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);

    @Query("SELECT p FROM Product p WHERE p.quantity <= p.reorderThreshold ORDER BY p.quantity ASC")
    List<Product> findLowStockProducts();

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<Product> search(@Param("q") String q);

    @Query("SELECT c.name, SUM(p.price * p.quantity) FROM Product p JOIN p.category c GROUP BY c.name ORDER BY SUM(p.price * p.quantity) DESC")
    List<Object[]> getInventoryValueByCategory();

    @Query("SELECT s.name, COUNT(p), SUM(p.price * p.quantity) FROM Product p JOIN p.supplier s GROUP BY s.name ORDER BY SUM(p.price * p.quantity) DESC")
    List<Object[]> getInventoryBySupplier();
}
