package com.pharmavita.pharmacy_backend.models.records;

import com.pharmavita.pharmacy_backend.models.Role;

public record UserProfile(String firstname, String lastname, String email, Role role) {};

