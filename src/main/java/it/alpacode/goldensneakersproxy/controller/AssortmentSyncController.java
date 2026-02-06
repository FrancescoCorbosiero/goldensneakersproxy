package it.alpacode.goldensneakersproxy.controller;

import it.alpacode.goldensneakersproxy.service.AssortmentMapperService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for syncing Golden Sneakers assortment into WooCommerce products.
 * Maps external feed → WooCommerce structure, resolving taxonomy IDs and uploading media.
 *
 * Base path: /assortment-sync
 */
@RestController
@RequestMapping("/assortment-sync")
public class AssortmentSyncController {

    private static final Logger logger = LoggerFactory.getLogger(AssortmentSyncController.class);

    private final AssortmentMapperService mapperService;

    public AssortmentSyncController(AssortmentMapperService mapperService) {
        this.mapperService = mapperService;
    }

    // ========== SYNC (persist) ==========

    /**
     * POST /assortment-sync/full - Sync full GS assortment into WooCommerce products.
     * Fetches all products from GS API, maps to Woo structure, and upserts.
     */
    @PostMapping(value = "/full", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AssortmentMapperService.SyncResult> syncFullAssortment() {
        logger.info("POST /assortment-sync/full - Starting full assortment sync");
        AssortmentMapperService.SyncResult result = mapperService.syncFullAssortment();
        return ResponseEntity.ok(result);
    }

    /**
     * POST /assortment-sync/product/{gsProductId} - Sync a single GS product.
     */
    @PostMapping(value = "/product/{gsProductId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AssortmentMapperService.SyncResult> syncProduct(@PathVariable Integer gsProductId) {
        logger.info("POST /assortment-sync/product/{} - Syncing single product", gsProductId);
        AssortmentMapperService.SyncResult result = mapperService.syncProductById(gsProductId);
        return ResponseEntity.ok(result);
    }

    // ========== PREVIEW (dry run) ==========

    /**
     * GET /assortment-sync/preview - Preview mapping of full assortment without saving.
     */
    @GetMapping(value = "/preview", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AssortmentMapperService.MappedProduct>> previewFullAssortment() {
        logger.info("GET /assortment-sync/preview - Previewing full assortment mapping");
        List<AssortmentMapperService.MappedProduct> result = mapperService.previewFullAssortment();
        return ResponseEntity.ok(result);
    }

    /**
     * GET /assortment-sync/preview/{gsProductId} - Preview mapping of a single product.
     */
    @GetMapping(value = "/preview/{gsProductId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AssortmentMapperService.MappedProduct> previewProduct(@PathVariable Integer gsProductId) {
        logger.info("GET /assortment-sync/preview/{} - Previewing product mapping", gsProductId);
        AssortmentMapperService.MappedProduct result = mapperService.previewProductById(gsProductId);
        return ResponseEntity.ok(result);
    }
}
