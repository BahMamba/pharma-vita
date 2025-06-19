package com.pharmavita.pharmacy_backend.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.pharmavita.pharmacy_backend.models.AuditLog;
import com.pharmavita.pharmacy_backend.models.Product;
import com.pharmavita.pharmacy_backend.models.records.AuditLogRequest;
import com.pharmavita.pharmacy_backend.models.records.ProductRequest;
import com.pharmavita.pharmacy_backend.models.records.StockUpdateRequest;
import com.pharmavita.pharmacy_backend.repositories.AuditLogRepository;
import com.pharmavita.pharmacy_backend.repositories.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductManageService {

    private final ProductRepository productRepository;
    private final AuditLogRepository auditLogRepository;

    // methode utilitaires 

    private void validateProduct(ProductRequest request){
        if (request.expirationDate().isBefore(request.manufacturingDate()) && (request.expirationDate().isBefore(LocalDate.now()))) {
            throw new IllegalArgumentException("Date d'expiration invalide"); 
        }

    }

    private void auditLogAction(AuditLogRequest request){
        AuditLog log = new AuditLog();
        log.setEntityId(request.entityId());
        log.setEntityType(request.entityType());
        log.setDetails(request.details());
        log.setAction(request.actionType());
        log.setPerformedBy(request.performedBy());
        log.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(log);
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Produit non trouvé"));
    }

    // Methode Pour la creation d'un Product(Medicament)
    public Product createProduct(ProductRequest request, Authentication authentication){
        validateProduct(request);

        Product product = new Product();
        product.setName(request.name());
        product.setDescription(request.description());
        product.setCategory(request.category());
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setManufacturingDate(request.manufacturingDate());
        product.setExpirationDate(request.expirationDate());
        
        product = productRepository.save(product);

        AuditLogRequest logRequest = new AuditLogRequest (product.getId(), "Product", "CREATION/AJOUT", "Creation de produit " + product.getName(), authentication.getName());
        auditLogAction(logRequest);
        
        return product;
    }

    // Methode pour mise a jour de Product
    public Product updateProduct(Long id, ProductRequest request, Authentication authentication){
        Product product = getProductById(id);

        validateProduct(request);

        String oldInfo = "Nom: " + product.getName() + ", Stock: " + product.getStock();

        product.setName(request.name());
        product.setDescription(request.description());
        product.setCategory(request.category());
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setManufacturingDate(request.manufacturingDate());
        product.setExpirationDate(request.expirationDate());

        product = productRepository.save(product);

        String newInfo = "Nom: " + product.getName() + ", Stock: " + product.getStock();

        AuditLogRequest logRequest = new AuditLogRequest(product.getId(), "PRODUCT", "UPDATE du produit: " + product.getName(), "Avant: " + oldInfo + " Now: " + newInfo, authentication.getName());

        auditLogAction(logRequest);

        return product;
    }

    // Methode pour recupeper tout les products avec filter/trie and pagination
    public Page<Product> getAllProducts(String filter, Pageable pageable) {
        if (filter == null || filter.isBlank()) {
            return productRepository.findAll(pageable);
        }

        Page<Product> filteredProducts;

        try {
            int stockFilter = Integer.parseInt(filter);
            filteredProducts = productRepository.findByStockGreaterThan(stockFilter, pageable);
            if (filteredProducts.isEmpty()) {
                filteredProducts = productRepository.findByStockLessThan(stockFilter, pageable);
            }
        } catch (NumberFormatException e) {
            filteredProducts = productRepository.findByNameContainingAllIgnoreCase(filter, pageable);
            if (filteredProducts.isEmpty()) {
                filteredProducts = productRepository.findByCategoryAllIgnoreCase(filter, pageable);
            }
        }

        return filteredProducts;
    }

    // Methode pour la suppression d'un product
    public void deleteProduct(Long id, Authentication authentication){
        Product product = getProductById(id);
        productRepository.deleteById(id);

        AuditLogRequest logRequest = new AuditLogRequest (product.getId(), "Product", "SUPPRESSION", "Produit " + product.getName() + " Supprimer", authentication.getName());
        auditLogAction(logRequest);
    }

    // Methode pour gerer l'approvisionnement d'un product en rupture de stock
    public Product updateStock(Long id, StockUpdateRequest request, Authentication authentication){
        Product product = getProductById(id);
        int stockValue = product.getStock() + request.stockChange();
        if (stockValue <= 0) {
            throw new IllegalArgumentException("Stock Invalide");
        }
        product.setStock(stockValue);
        productRepository.save(product);

        AuditLogRequest logRequest = new AuditLogRequest ( id, "PRODUCT", "STOCK_UPDATE",
            "Stock changé de " + (stockValue - request.stockChange()) + " à " + stockValue + ", Raison: " + request.reason(),
            authentication.getName());
        auditLogAction(logRequest);

        return product;

    }


}
