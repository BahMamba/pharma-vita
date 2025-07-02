package com.pharmavita.pharmacy_backend.repositories;

import com.pharmavita.pharmacy_backend.models.ProductCategory;
import com.pharmavita.pharmacy_backend.models.Product;
import com.pharmavita.pharmacy_backend.models.ProductStatus;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Product> findByCategory(ProductCategory category, Pageable pageable);
    Page<Product> findByStatus(ProductStatus status, Pageable pageable);
    Page<Product> findByStockGreaterThan(int stock, Pageable pageable);
    Page<Product> findByStockLessThan(int stock, Pageable pageable);
    List<Product> findByIdIn(List<Long> ids);
}