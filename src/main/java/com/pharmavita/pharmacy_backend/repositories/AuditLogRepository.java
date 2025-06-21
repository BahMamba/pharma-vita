package com.pharmavita.pharmacy_backend.repositories;

import com.pharmavita.pharmacy_backend.models.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByPerformedBy(String performedBy);
    List<AuditLog> findByEntityIdAndEntityTypeAndAction(Long entityId, String entityType, String action);
}