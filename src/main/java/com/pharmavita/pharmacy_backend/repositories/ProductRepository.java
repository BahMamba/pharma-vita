package com.pharmavita.pharmacy_backend.repositories;

import java.util.Locale.Category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.pharmavita.pharmacy_backend.models.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Product> findByCategory(Category category, Pageable pageable);
    Page<Product> findByStockGreaterThan(int stock, Pageable pageable);
    Page<Product> findByStockLessThan(int stock, Pageable pageable);
}
