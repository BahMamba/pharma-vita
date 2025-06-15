package com.pharmavita.pharmacy_backend.controllers;

import com.pharmavita.pharmacy_backend.models.User;
import com.pharmavita.pharmacy_backend.models.records.UserRequest;
import com.pharmavita.pharmacy_backend.services.UserPharmaManagerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {
    private final UserPharmaManagerService userPharmaManagerService;

    @PostMapping
    public ResponseEntity<User> createPharmacist(@Valid @RequestBody UserRequest request, Authentication authentication) throws Exception {
        User user = userPharmaManagerService.createPharmacist(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllPharmacist() {
        return ResponseEntity.ok(userPharmaManagerService.listPharmacist());
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updatePharmacist(@PathVariable Long id, @Valid @RequestBody UserRequest request, Authentication authentication) throws Exception {
        User user = userPharmaManagerService.updatePharmacist(id, request, authentication.getName());
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePharmacist(@PathVariable Long id, Authentication authentication) {
        userPharmaManagerService.deletePharmacist(id, authentication.getName());
        return ResponseEntity.ok().build();
    }
}