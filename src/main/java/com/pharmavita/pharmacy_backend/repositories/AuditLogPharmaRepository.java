package com.pharmavita.pharmacy_backend.repositories;

import com.pharmavita.pharmacy_backend.models.AuditLogPharma;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogPharmaRepository extends JpaRepository<AuditLogPharma, Long> {
    List<AuditLogPharma> findByPerformedBy(String performedBy);
}