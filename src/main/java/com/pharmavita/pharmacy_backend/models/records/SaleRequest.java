package com.pharmavita.pharmacy_backend.models.records;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

// Requête pour créer une vente
public record SaleRequest(
    @NotEmpty(message = "Au moins un produit requis")
    List<SaleItemRequest> items
) {}

// Un item dans la vente (produit et quantité)
// Moved to SaleItemRequest.java