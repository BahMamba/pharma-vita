package com.pharmavita.pharmacy_backend.models.records;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SaleItemRequest(
    @NotNull(message = "Produit requis")
    Long productId,
    @Positive(message = "Quantité invalide")
    int quantity
) {}
