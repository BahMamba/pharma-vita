package com.pharmavita.pharmacy_backend.models.records;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record StockUpdateRequest(
    @NotNull(message = "Quantité requise") Integer stockChange,
    @NotBlank(message = "Raison requise") String reason,
    LocalDate replenishmentDate 
) {}