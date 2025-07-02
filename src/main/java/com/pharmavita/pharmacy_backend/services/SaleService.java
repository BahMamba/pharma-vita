package com.pharmavita.pharmacy_backend.services;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
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

    private static final String ENTITY_TYPE = "SALE";
    private static final String ACTION_CREATE = "CREATE";
    private static final String ACTION_VIEW = "VIEW";
    private static final String ACTION_RECEIPT = "RECEIPT";

    public void auditLogAction(Long id, String actionType, String details, String performedBy) {
        AuditLogPharma log = new AuditLogPharma();
        log.setEntityId(id);
        log.setEntityType(ENTITY_TYPE);
        log.setDetails(details);
        log.setActionType(actionType);
        log.setPerformedBy(performedBy);
        log.setTimestamp(LocalDateTime.now());
        auditLogPharmaRepository.save(log);
    }

    public Sale createSale(SaleRequest request, Authentication auth) {
        Sale sale = new Sale();
        sale.setPerformedBy(auth.getName());
        sale.setSaleDate(LocalDateTime.now());
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<SaleItem> items = new ArrayList<>();
        for (SaleItemRequest itemRequest : request.items()) {
            Product product = productService.getProductById(itemRequest.productId());
            productService.sellProduct(itemRequest.productId(), itemRequest.quantity());
            SaleItem item = new SaleItem();
            item.setSale(sale);
            item.setProduct(product);
            item.setQuantity(itemRequest.quantity());
            item.setUnitPrice(product.getPrice());
            BigDecimal itemTotal = product.getPrice().multiply(new BigDecimal(itemRequest.quantity()));
            totalAmount = totalAmount.add(itemTotal);
            items.add(item);
        }
        sale.setItems(items);
        sale.setSaleAmount(totalAmount);
        sale = saleRepository.save(sale);
        auditLogAction(sale.getId(), ACTION_CREATE, "Vente de " + items.size() + " items, total: " + totalAmount, auth.getName());
        return sale;
    }

    // Nouvelle méthode pour créer un brouillon de vente
    public Sale createDraftSale(SaleRequest request, Authentication auth) {
        Sale sale = new Sale();
        sale.setPerformedBy(auth.getName());
        sale.setSaleDate(LocalDateTime.now());
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<SaleItem> items = new ArrayList<>();
        for (SaleItemRequest itemRequest : request.items()) {
            Product product = productService.getProductById(itemRequest.productId());
            // Vérifier le stock sans le modifier
            if (product.getStock() < itemRequest.quantity()) {
                throw new IllegalArgumentException("Stock insuffisant pour le produit " + product.getName());
            }
            SaleItem item = new SaleItem();
            item.setSale(sale);
            item.setProduct(product);
            item.setQuantity(itemRequest.quantity());
            item.setUnitPrice(product.getPrice());
            BigDecimal itemTotal = product.getPrice().multiply(new BigDecimal(itemRequest.quantity()));
            totalAmount = totalAmount.add(itemTotal);
            items.add(item);
        }
        sale.setItems(items);
        sale.setSaleAmount(totalAmount);
        return sale; // Ne pas sauvegarder, juste retourner le brouillon
    }

    public Page<Sale> getAllSales(String performedBy, Pageable pageable) {
        if (performedBy != null) {
            return saleRepository.findByPerformedBy(performedBy, pageable);
        }
        return saleRepository.findAll(pageable);
    }

    public Sale getSale(Long id, Authentication authentication) {
        Sale sale = saleRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Vente non trouvee"));
        return sale;
    }

    public byte[] generateReceipt(Long id, Authentication authentication) {
        Sale sale = getSale(id, authentication);
        try {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            PdfWriter pdfWriter = new PdfWriter(b);
            PdfDocument pdfDocument = new PdfDocument(pdfWriter);
            Document document = new Document(pdfDocument);
            document.add(new Paragraph("PharmaVita - Reçu de vente"));
            document.add(new Paragraph("Vente ID: " + sale.getId()));
            document.add(new Paragraph("Pharmacien: " + sale.getPerformedBy()));
            document.add(new Paragraph("Date: " + sale.getSaleDate()));
            Table table = new Table(new float[]{3, 1, 2, 2});
            table.addCell("Produit");
            table.addCell("Quantité");
            table.addCell("Prix unitaire");
            table.addCell("Total");
            for (SaleItem item : sale.getItems()) {
                table.addCell(item.getProduct().getName());
                table.addCell(String.valueOf(item.getQuantity()));
                table.addCell(item.getUnitPrice().toString() + " GNF");
                BigDecimal itemTotal = item.getUnitPrice().multiply(new BigDecimal(item.getQuantity()));
                table.addCell(itemTotal.toString() + " GNF");
            }
            document.add(table);
            document.add(new Paragraph("Total: " + sale.getSaleAmount() + " GNF"));
            document.close();
            auditLogAction(id, ACTION_RECEIPT, "Génération reçu PDF", authentication.getName());
            return b.toByteArray();
        } catch (Exception e) {
            throw new IllegalArgumentException("Erreur de generation du receipt: " + e.getMessage());
        }
    }
}