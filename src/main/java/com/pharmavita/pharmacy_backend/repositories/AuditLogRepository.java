package com.pharmavita.pharmacy_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pharmavita.pharmacy_backend.models.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long>{}
