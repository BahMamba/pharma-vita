package com.pharmavita.pharmacy_backend.controllers;

import com.pharmavita.pharmacy_backend.models.records.LoginRequest;
import com.pharmavita.pharmacy_backend.models.records.UserProfile;
import com.pharmavita.pharmacy_backend.services.AuthService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest request) {
        String token = authService.login(request);
        return ResponseEntity.status(HttpStatus.OK).body((Map.of("token", token)));
    }

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfile> getProfile(Authentication authentication){
        UserProfile userProfile = authService.getProfile(authentication.getName());
        return ResponseEntity.ok(userProfile);        
    }
}