package it.alpacode.goldensneakersproxy.controller;

import it.alpacode.goldensneakersproxy.controller.dto.CatalogProductRequest;
import it.alpacode.goldensneakersproxy.controller.dto.SyncResultResponse;
import it.alpacode.goldensneakersproxy.exception.CatalogSyncException;
import it.alpacode.goldensneakersproxy.model.CatalogProduct;
import it.alpacode.goldensneakersproxy.model.SyncResult;
import it.alpacode.goldensneakersproxy.service.CatalogSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for catalog synchronization.
 * Simple bulk operations.
 */
@RestController
@RequestMapping("/api/catalog")
public class CatalogController {

    private static final Logger log = LoggerFactory.getLogger(CatalogController.class);

    private final CatalogSyncService syncService;

    public CatalogController(CatalogSyncService syncService) {
        this.syncService = syncService;
    }

    /**
     * Sync catalog - creates new, updates existing.
     * POST /api/catalog/sync
     */
    @PostMapping("/sync")
    public ResponseEntity<SyncResultResponse> syncCatalog(
            @RequestBody List<CatalogProductRequest> feed) {

        log.info("Sync request: {} products", feed.size());

        if (feed.isEmpty()) {
            SyncResult empty = new SyncResult();
            empty.setStatus("SUCCESS");
            empty.addWarning("Empty feed");
            return ResponseEntity.ok(SyncResultResponse.from(empty));
        }

        List<CatalogProduct> products = feed.stream()
                .map(CatalogProductRequest::toDomain)
                .collect(Collectors.toList());

        SyncResult result = syncService.syncCatalog(products);
        return ResponseEntity.ok(SyncResultResponse.from(result));
    }

    /**
     * Mark products as out of stock.
     * POST /api/catalog/mark-out-of-stock
     */
    @PostMapping("/mark-out-of-stock")
    public ResponseEntity<Map<String, Object>> markOutOfStock(
            @RequestBody List<Long> productIds) {

        log.info("Mark out of stock: {} products", productIds.size());

        int count = syncService.markOutOfStock(productIds);

        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "markedOutOfStock", count
        ));
    }

    /**
     * Find products in shop but not in feed.
     * POST /api/catalog/find-missing
     */
    @PostMapping("/find-missing")
    public ResponseEntity<Map<String, Object>> findMissing(
            @RequestBody List<CatalogProductRequest> feed) {

        log.info("Find missing: comparing {} feed products", feed.size());

        List<CatalogProduct> products = feed.stream()
                .map(CatalogProductRequest::toDomain)
                .collect(Collectors.toList());

        List<Long> missing = syncService.findMissingProducts(products);

        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "missingProductIds", missing,
                "count", missing.size()
        ));
    }

    @ExceptionHandler(CatalogSyncException.class)
    public ResponseEntity<SyncResultResponse> handleSyncException(CatalogSyncException e) {
        log.error("Sync failed: {}", e.getMessage());

        SyncResult failed = new SyncResult();
        failed.setStatus("FAILED");
        failed.setErrorMessage(e.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(SyncResultResponse.from(failed));
    }
}
