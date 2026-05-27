package com.stockbase.repository;

import com.stockbase.model.InventoryTransaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<InventoryTransaction, Long> {

    List<InventoryTransaction> findByProductIdOrderByCreatedAtDesc(Long productId);

    List<InventoryTransaction> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
