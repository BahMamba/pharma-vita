package com.pharmavita.pharmacy_backend.models.records;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StockUpdateRequest(
    @NotNull int stockChange,
    @NotBlank(message = "Raison requise") String reason
) {}
