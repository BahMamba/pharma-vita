package com.pharmavita.pharmacy_backend.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pharmavita.pharmacy_backend.models.Sale;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long>{
    Page<Sale>findByPerformedBy(String performedBy, Pageable pageable);
}
