package com.pharmavita.pharmacy_backend.models.records;

import com.pharmavita.pharmacy_backend.models.Role;

public record RegisterRequest(String firstname, String lastname, String email, String password, Role role) {}