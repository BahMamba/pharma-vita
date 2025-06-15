package com.pharmavita.pharmacy_backend.models.records;

import com.pharmavita.pharmacy_backend.models.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserRequest(
    @NotBlank(message = "Le nom est requis")
    String firstname,
    
    @NotBlank(message = "Le prenom est requis")
    String lastname,
    
    @NotBlank(message = "Email requis avec le format valide")
    @Email(message = "Format d'email invalide")
    String email,
     
    Role role
    ) {}