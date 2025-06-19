package com.pharmavita.pharmacy_backend.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pharmavita.pharmacy_backend.models.Product;
import com.pharmavita.pharmacy_backend.models.records.ProductRequest;
import com.pharmavita.pharmacy_backend.models.records.StockUpdateRequest;
import com.pharmavita.pharmacy_backend.services.ProductManageService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductManageController {
    private final ProductManageService productManageService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> createProduct(@Valid @RequestBody ProductRequest request, Authentication authentication){
        Product product = productManageService.createProduct(request, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);

    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest request, Authentication authentication){
        Product product = productManageService.updateProduct(id, request, authentication);
        return ResponseEntity.status(HttpStatus.OK).body(product);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public ResponseEntity<Page<Product>> getAllProducts(
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size,
                                            @RequestParam(defaultValue = "name") String sortBy,
                                            @RequestParam(required = false) String filter
    ){
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));

        return ResponseEntity.ok(productManageService.getAllProducts(filter, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public ResponseEntity<Product> getProduct(@PathVariable Long id){
        Product product = productManageService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> updateStock(@PathVariable Long id, @Valid @RequestBody StockUpdateRequest request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(productManageService.updateStock(id, request, authentication));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id, Authentication authentication){
        productManageService.deleteProduct(id, authentication);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


}
