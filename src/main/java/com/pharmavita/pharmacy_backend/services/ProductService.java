package com.pharmavita.pharmacy_backend.services;

import com.pharmavita.pharmacy_backend.models.AuditLog;
import com.pharmavita.pharmacy_backend.models.ProductCategory;
import com.pharmavita.pharmacy_backend.models.Product;
import com.pharmavita.pharmacy_backend.models.ProductStatus;
import com.pharmavita.pharmacy_backend.models.records.ProductRequest;
import com.pharmavita.pharmacy_backend.models.records.StockUpdateRequest;
import com.pharmavita.pharmacy_backend.repositories.AuditLogRepository;
import com.pharmavita.pharmacy_backend.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final AuditLogRepository auditLogRepository;

    private static final String ENTITY_TYPE = "PRODUCT";
    private static final String ACTION_CREATE = "CREATE";
    private static final String ACTION_UPDATE = "UPDATE";
    private static final String ACTION_DELETE = "DELETE";
    private static final String ACTION_STOCK = "STOCK_UPDATE";
    private static final int LOW_STOCK_THRESHOLD = 10;

    private void validateProduct(ProductRequest request) {
        if (request.expirationDate().isBefore(request.manufacturingDate())) {
            throw new IllegalArgumentException("Date d'expiration après fabrication");
        }
        if (request.expirationDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Date d'expiration future");
        }
    }

    private ProductStatus determineStatus(int stock) {
        if (stock == 0) return ProductStatus.OUT_OF_STOCK;
        if (stock < LOW_STOCK_THRESHOLD) return ProductStatus.LOW_STOCK;
        return ProductStatus.AVAILABLE;
    }

    private void auditLogAction(Long id, String actionType, String details, String performedBy) {
        AuditLog log = new AuditLog();
        log.setEntityId(id);
        log.setEntityType(ENTITY_TYPE);
        log.setAction(actionType); 
        log.setDetails(details);
        log.setPerformedBy(performedBy);
        log.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(log);
    }

    public void sellProduct(Long id, int quantity){
        Product product = getProductById(id);
        
        if (product.getStock() < quantity) {
            throw new IllegalArgumentException("Pas assez de stock pour " + product.getName());
        }
        product.setStock(product.getStock() - quantity);

        product.setStatus(determineStatus(product.getStock()));

        productRepository.save(product);
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Produit non trouvé"));
    }

    public Product createProduct(ProductRequest request, Authentication auth) {
        validateProduct(request);
        Product product = new Product();
        product.setName(request.name());
        product.setDescription(request.description());
        product.setCategory(request.category());
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setStatus(determineStatus(request.stock()));
        product.setManufacturingDate(request.manufacturingDate());
        product.setExpirationDate(request.expirationDate());
        product = productRepository.save(product);
        auditLogAction(product.getId(), ACTION_CREATE, "Création: " + product.getName(), auth.getName());
        return product;
    }

    public Product updateProduct(Long id, ProductRequest request, Authentication auth) {
        Product product = getProductById(id);
        validateProduct(request);
        String oldInfo = "Nom: " + product.getName() + ", Stock: " + product.getStock() + ", Status: " + product.getStatus();
        product.setName(request.name());
        product.setDescription(request.description());
        product.setCategory(request.category());
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setStatus(determineStatus(request.stock()));
        product.setManufacturingDate(request.manufacturingDate());
        product.setExpirationDate(request.expirationDate());
        product = productRepository.save(product);
        String newInfo = "Nom: " + product.getName() + ", Stock: " + product.getStock() + ", Status: " + product.getStatus();
        auditLogAction(id, ACTION_UPDATE, "Avant: " + oldInfo + "; Après: " + newInfo, auth.getName());
        return product;
    }

    public Page<Product> getProductsForSale(String filter, Pageable pageable) {
        if (filter == null || filter.isBlank()) {
            return productRepository.findAll(pageable);
        }
        if (filter.startsWith("category:")) {
            String category = filter.substring(9).toUpperCase();
            return productRepository.findByCategory(ProductCategory.valueOf(category), pageable);
        }
        return productRepository.findByNameContainingIgnoreCase(filter, pageable);
    }

    public Page<Product> getProductsForRestock(String filter, Pageable pageable) {
        if (filter == null || filter.isBlank()) {
            return productRepository.findAll(pageable); 
        }
        if (filter.startsWith("status:")) {
            String status = filter.substring(7).toUpperCase();
            return productRepository.findByStatus(ProductStatus.valueOf(status), pageable);
        }
        try {
            int stockFilter = Integer.parseInt(filter);
            Page<Product> products = productRepository.findByStockLessThan(stockFilter, pageable);
            if (!products.isEmpty()) return products;
            return productRepository.findByStockGreaterThan(stockFilter, pageable);
        } catch (NumberFormatException e) {
            return productRepository.findAll(pageable); 
        }
    }

    public void deleteProduct(Long id, Authentication auth) {
        Product product = getProductById(id);
        productRepository.deleteById(id);
        auditLogAction(id, ACTION_DELETE, "Suppression: " + product.getName(), auth.getName());
    }

    public Product updateStock(Long id, StockUpdateRequest request, Authentication auth) {
        Product product = getProductById(id);
        int newStock = product.getStock() + request.stockChange();
        if (newStock < 0) {
            throw new IllegalArgumentException("Stock ne peut pas être négatif");
        }

        product.setStock(newStock);
        product.setStatus(determineStatus(newStock));
        product = productRepository.save(product);

        StockUpdateRequest updatedRequest = new StockUpdateRequest(
            request.stockChange(),
            request.reason(),
            LocalDate.now() 
        );

        auditLogAction(
            id,
            ACTION_STOCK,
            String.format("Stock: %d -> %d, Raison: %s, Date: %s",
                product.getStock() - request.stockChange(),
                newStock,
                updatedRequest.reason(),
                updatedRequest.replenishmentDate()),
            auth.getName()
        );

        return product;
    }
     
    
        
}
    
    
