package com.pharmavita.pharmacy_backend.models.records;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.pharmavita.pharmacy_backend.models.ProductCategory;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;

public record ProductRequest(
    @NotBlank(message = "Nom requis") String name,
    String description,
    @NotNull @PositiveOrZero(message = "Prix non valide") BigDecimal price,
    @NotNull @PositiveOrZero(message = "Stock non valide") Integer stock,
    @NotNull(message = "Catégorie requise") ProductCategory category,    @NotNull @PastOrPresent(message = "Date de fabrication invalide") LocalDate manufacturingDate,
    @NotNull @FutureOrPresent(message = "Date d’expiration invalide") LocalDate expirationDate
) {}
