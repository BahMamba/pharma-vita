package com.pharmavita.pharmacy_backend.models.records;

public record AuditLogRequest(
    Long entityId,
    String entityType,
    String actionType,
    String details,
    String performedBy
) {}
