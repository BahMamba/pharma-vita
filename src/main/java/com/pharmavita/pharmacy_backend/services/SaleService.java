package com.pharmavita.pharmacy_backend.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.pharmavita.pharmacy_backend.models.AuditLogPharma;
import com.pharmavita.pharmacy_backend.models.Product;
import com.pharmavita.pharmacy_backend.models.Sale;
import com.pharmavita.pharmacy_backend.models.SaleItem;
import com.pharmavita.pharmacy_backend.models.records.SaleItemRequest;
import com.pharmavita.pharmacy_backend.models.records.SaleRequest;
import com.pharmavita.pharmacy_backend.repositories.AuditLogPharmaRepository;
import com.pharmavita.pharmacy_backend.repositories.SaleItemRepository;
import com.pharmavita.pharmacy_backend.repositories.SaleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SaleService {
    private final ProductService productService;
    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final AuditLogPharmaRepository auditLogPharmaRepository;

    // Constantes pour les logs
    private static final String ENTITY_TYPE = "SALE";
    private static final String ACTION_CREATE = "CREATE";
    private static final String ACTION_VIEW = "VIEW";
    private static final String ACTION_RECEIPT = "RECEIPT";

    // Methodes utilitaire

    public void auditLogAction(Long id, String actionType, String details, String performedBy){
        AuditLogPharma log = new AuditLogPharma();
        log.setEntityId(id);
        log.setEntityType(ENTITY_TYPE);
        log.setDetails(details);
        log.setActionType(actionType);
        log.setPerformedBy(performedBy);
        log.setTimestamp(LocalDateTime.now());
        auditLogPharmaRepository.save(log);
    }

    public Sale createSale(SaleRequest request, Authentication authentication){
        Sale sale = new Sale();
        sale.setPerformedBy(authentication.getName());
        sale.setSaleAmount(BigDecimal.ZERO);
        sale.setSaleDate(LocalDateTime.now());
        List<SaleItem> items = new ArrayList<>();

        for (SaleItemRequest saleItem : request.items()) {
            Product product = productService.getProductById(saleItem.productId());

            productService.sellProduct(saleItem.productId(), saleItem.quantity());
            
            SaleItem item = new SaleItem();
            item.setSale(sale);
            item.setProduct(product);
            item.setQuantity(saleItem.quantity());
            item.setUnitPrice(product.getPrice());

            BigDecimal itemPrice = product.getPrice().multiply(new BigDecimal(saleItem.quantity()));
            sale.setSaleAmount(itemPrice);

            items.add(item);
        }

        sale.setItems(items);

        auditLogAction(sale.getId(), ACTION_CREATE, "Vente de " + items.size() + " produits, total: " + sale.getSaleAmount(), authentication.getName());
        
        return sale;
    }

    

}
