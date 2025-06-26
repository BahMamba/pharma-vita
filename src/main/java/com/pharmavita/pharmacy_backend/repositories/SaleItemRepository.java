package com.pharmavita.pharmacy_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pharmavita.pharmacy_backend.models.SaleItem;

@Repository
public interface SaleItemRepository extends JpaRepository<SaleItem, Long>{

}
