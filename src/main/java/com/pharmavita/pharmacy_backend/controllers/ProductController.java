package com.pharmavita.pharmacy_backend.controllers;

import com.pharmavita.pharmacy_backend.models.ProductCategory;
import com.pharmavita.pharmacy_backend.models.Product;
import com.pharmavita.pharmacy_backend.models.records.ProductRequest;
import com.pharmavita.pharmacy_backend.models.records.StockUpdateRequest;
import com.pharmavita.pharmacy_backend.services.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> createProduct(@Valid @RequestBody ProductRequest request, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(request, auth));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest request, Authentication auth) {
        return ResponseEntity.ok(productService.updateProduct(id, request, auth));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public ResponseEntity<Page<Product>> getProductsForSale(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "name") String sortBy,
        @RequestParam(required = false) String filter) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        return ResponseEntity.ok(productService.getProductsForSale(filter, pageable));
    }

    @GetMapping("/restock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Product>> getProductsForRestock(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "stock") String sortBy,
        @RequestParam(required = false) String filter) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        return ResponseEntity.ok(productService.getProductsForRestock(filter, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> updateStock(
        @PathVariable Long id,
        @Valid @RequestBody StockUpdateRequest request,
        Authentication auth
    ) {
        Product updatedProduct = productService.updateStock(id, request, auth);
        return ResponseEntity.accepted().body(updatedProduct);
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id, Authentication auth) {
        productService.deleteProduct(id, auth);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/categories")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public ResponseEntity<List<String>> getCategories() {
        List<String> categories = Arrays.stream(ProductCategory.values()).map(Enum::name).toList();
        return ResponseEntity.ok(categories);
    }
}