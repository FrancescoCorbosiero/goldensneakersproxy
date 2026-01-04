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
import java.util.stream.Collectors;

/**
 * REST controller for catalog synchronization operations.
 * All operations are bulk-focused.
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
     * Synchronize the entire catalog.
     *
     * POST /api/catalog/sync
     *
     * Body: Array of CatalogProductRequest
     *
     * Response: SyncResultResponse
     */
    @PostMapping("/sync")
    public ResponseEntity<SyncResultResponse> syncCatalog(
            @RequestBody List<CatalogProductRequest> feed) {

        log.info("Received sync request with {} products", feed.size());

        if (feed.isEmpty()) {
            log.warn("Empty feed received, nothing to sync");
            SyncResult emptyResult = new SyncResult();
            emptyResult.setStatus("SUCCESS");
            emptyResult.addWarning("Empty feed - no products to sync");
            return ResponseEntity.ok(SyncResultResponse.from(emptyResult));
        }

        List<CatalogProduct> products = feed.stream()
                .map(CatalogProductRequest::toDomain)
                .collect(Collectors.toList());

        SyncResult result = syncService.syncCatalog(products);

        return ResponseEntity.ok(SyncResultResponse.from(result));
    }

    /**
     * Handle sync exceptions.
     */
    @ExceptionHandler(CatalogSyncException.class)
    public ResponseEntity<SyncResultResponse> handleSyncException(CatalogSyncException e) {
        log.error("Catalog sync failed: {}", e.getMessage());

        SyncResult failedResult = new SyncResult();
        failedResult.setStatus("FAILED");
        failedResult.setErrorMessage(e.getMessage());
        if (e.isRolledBack()) {
            failedResult.addWarning("Transaction was rolled back");
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(SyncResultResponse.from(failedResult));
    }
}
