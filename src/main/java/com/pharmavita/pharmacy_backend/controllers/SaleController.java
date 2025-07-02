package com.pharmavita.pharmacy_backend.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pharmavita.pharmacy_backend.models.Sale;
import com.pharmavita.pharmacy_backend.models.records.SaleRequest;
import com.pharmavita.pharmacy_backend.services.SaleService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SaleController {
    private final SaleService saleService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public ResponseEntity<Sale> createSale(@Valid @RequestBody SaleRequest request, Authentication authentication) {
        Sale sale = saleService.createSale(request, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(sale);
    }

    @PostMapping("/draft")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public ResponseEntity<Sale> createDraftSale(@Valid @RequestBody SaleRequest request, Authentication authentication) {
        Sale sale = saleService.createDraftSale(request, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(sale);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public ResponseEntity<Page<Sale>> getAllSales(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "saleDate") String sortBy,
        Authentication auth) {
        String performedBy = auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")) ? null : auth.getName();
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        return ResponseEntity.ok(saleService.getAllSales(performedBy, pageable));
    }

    @GetMapping("/my-sales")
    @PreAuthorize("hasRole('PHARMACIST')")
    public ResponseEntity<Page<Sale>> getMySales(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "saleDate") String sortBy,
        Authentication auth) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        return ResponseEntity.ok(saleService.getAllSales(auth.getName(), pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public ResponseEntity<Sale> getSale(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(saleService.getSale(id, auth));
    }

    @GetMapping("/{id}/receipt")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public ResponseEntity<byte[]> getReceipt(@PathVariable Long id, Authentication auth) {
        byte[] pdf = saleService.generateReceipt(id, auth);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "receipt-" + id + ".pdf");
        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }
}