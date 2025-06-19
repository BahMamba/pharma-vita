package com.pharmavita.pharmacy_backend.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "audit_logs_pharmacist")
public class AuditLogPharma {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String entityType; 

    @Column(nullable = false)
    private Long entityId; 

    @Column(nullable = false)
    private String actionType; 

    private String details;

    @Column(nullable = false)
    private String performedBy; 

    @Column(nullable = false)
    private LocalDateTime timestamp;
}