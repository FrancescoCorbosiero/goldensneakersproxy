package it.alpacode.goldensneakersproxy.controller;

import it.alpacode.goldensneakersproxy.entity.woocommerce.WooVariation;
import it.alpacode.goldensneakersproxy.service.WooVariationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for WooCommerce Product Variation CRUD operations.
 * Supports both standalone variations and product-scoped variations.
 */
@RestController
public class WooVariationController {

    private static final Logger logger = LoggerFactory.getLogger(WooVariationController.class);

    private final WooVariationService variationService;

    public WooVariationController(WooVariationService variationService) {
        this.variationService = variationService;
    }

    // ========== STANDALONE VARIATION ENDPOINTS (/woo/variations) ==========

    /**
     * POST /woo/variations - Create a new variation
     */
    @PostMapping(value = "/woo/variations",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WooVariation> createVariation(@RequestBody WooVariation variation) {
        logger.info("POST /woo/variations - Creating new variation for product ID: {}", variation.getProductId());
        WooVariation created = variationService.createVariation(variation);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * GET /woo/variations - List all variations with optional pagination
     */
    @GetMapping(value = "/woo/variations", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllVariations(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDir) {

        logger.info("GET /woo/variations - Fetching variations (page={}, size={}, sortBy={}, sortDir={})",
                page, size, sortBy, sortDir);

        if (page != null && size != null) {
            Sort sort = sortDir.equalsIgnoreCase("desc") ?
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<WooVariation> variationPage = variationService.getAllVariations(pageable);
            return ResponseEntity.ok(variationPage);
        }

        List<WooVariation> variations = variationService.getAllVariations();
        return ResponseEntity.ok(variations);
    }

    /**
     * GET /woo/variations/{id} - Get variation by ID
     */
    @GetMapping(value = "/woo/variations/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WooVariation> getVariationById(@PathVariable Long id) {
        logger.info("GET /woo/variations/{} - Fetching variation", id);
        return variationService.getVariationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /woo/variations/sku/{sku} - Get variation by SKU
     */
    @GetMapping(value = "/woo/variations/sku/{sku}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WooVariation> getVariationBySku(@PathVariable String sku) {
        logger.info("GET /woo/variations/sku/{} - Fetching variation", sku);
        return variationService.getVariationBySku(sku)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /woo/variations/status/{status} - Get variations by status
     */
    @GetMapping(value = "/woo/variations/status/{status}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<WooVariation>> getVariationsByStatus(@PathVariable String status) {
        logger.info("GET /woo/variations/status/{} - Fetching variations", status);
        List<WooVariation> variations = variationService.getVariationsByStatus(status);
        return ResponseEntity.ok(variations);
    }

    /**
     * GET /woo/variations/stock/{stockStatus} - Get variations by stock status
     */
    @GetMapping(value = "/woo/variations/stock/{stockStatus}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<WooVariation>> getVariationsByStockStatus(@PathVariable String stockStatus) {
        logger.info("GET /woo/variations/stock/{} - Fetching variations", stockStatus);
        List<WooVariation> variations = variationService.getVariationsByStockStatus(stockStatus);
        return ResponseEntity.ok(variations);
    }

    /**
     * PUT /woo/variations/{id} - Full update of a variation
     */
    @PutMapping(value = "/woo/variations/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WooVariation> updateVariation(
            @PathVariable Long id,
            @RequestBody WooVariation variation) {
        logger.info("PUT /woo/variations/{} - Updating variation", id);
        WooVariation updated = variationService.updateVariation(id, variation);
        return ResponseEntity.ok(updated);
    }

    /**
     * PATCH /woo/variations/{id} - Partial update of a variation
     */
    @PatchMapping(value = "/woo/variations/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WooVariation> patchVariation(
            @PathVariable Long id,
            @RequestBody WooVariation variation) {
        logger.info("PATCH /woo/variations/{} - Patching variation", id);
        WooVariation patched = variationService.patchVariation(id, variation);
        return ResponseEntity.ok(patched);
    }

    /**
     * DELETE /woo/variations/{id} - Delete a variation
     */
    @DeleteMapping("/woo/variations/{id}")
    public ResponseEntity<Void> deleteVariation(@PathVariable Long id) {
        logger.info("DELETE /woo/variations/{} - Deleting variation", id);
        variationService.deleteVariation(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * DELETE /woo/variations - Delete all variations (use with caution)
     */
    @DeleteMapping("/woo/variations")
    public ResponseEntity<Void> deleteAllVariations() {
        logger.warn("DELETE /woo/variations - Deleting all variations");
        variationService.deleteAllVariations();
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /woo/variations/stats - Get variation statistics
     */
    @GetMapping(value = "/woo/variations/stats", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getVariationStats() {
        logger.info("GET /woo/variations/stats - Fetching variation statistics");

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalVariations", variationService.countVariations());
        stats.put("publishedCount", variationService.countVariationsByStatus("publish"));
        stats.put("draftCount", variationService.countVariationsByStatus("draft"));
        stats.put("pendingCount", variationService.countVariationsByStatus("pending"));
        stats.put("privateCount", variationService.countVariationsByStatus("private"));

        return ResponseEntity.ok(stats);
    }

    // ========== PRODUCT-SCOPED VARIATION ENDPOINTS (/woo/products/{productId}/variations) ==========

    /**
     * POST /woo/products/{productId}/variations - Create a variation for a specific product
     */
    @PostMapping(value = "/woo/products/{productId}/variations",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WooVariation> createVariationForProduct(
            @PathVariable Long productId,
            @RequestBody WooVariation variation) {
        logger.info("POST /woo/products/{}/variations - Creating new variation", productId);
        WooVariation created = variationService.createVariation(productId, variation);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * GET /woo/products/{productId}/variations - List all variations for a specific product
     */
    @GetMapping(value = "/woo/products/{productId}/variations", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getVariationsForProduct(
            @PathVariable Long productId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {

        logger.info("GET /woo/products/{}/variations - Fetching variations (page={}, size={})",
                productId, page, size);

        if (page != null && size != null) {
            Pageable pageable = PageRequest.of(page, size);
            Page<WooVariation> variationPage = variationService.getVariationsByProductId(productId, pageable);
            return ResponseEntity.ok(variationPage);
        }

        List<WooVariation> variations = variationService.getVariationsByProductId(productId);
        return ResponseEntity.ok(variations);
    }

    /**
     * GET /woo/products/{productId}/variations/{id} - Get a specific variation for a product
     */
    @GetMapping(value = "/woo/products/{productId}/variations/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WooVariation> getVariationForProduct(
            @PathVariable Long productId,
            @PathVariable Long id) {
        logger.info("GET /woo/products/{}/variations/{} - Fetching variation", productId, id);
        return variationService.getVariationById(id)
                .filter(v -> productId.equals(v.getProductId()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /woo/products/{productId}/variations/status/{status} - Get variations by status for a product
     */
    @GetMapping(value = "/woo/products/{productId}/variations/status/{status}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<WooVariation>> getVariationsByProductIdAndStatus(
            @PathVariable Long productId,
            @PathVariable String status) {
        logger.info("GET /woo/products/{}/variations/status/{} - Fetching variations", productId, status);
        List<WooVariation> variations = variationService.getVariationsByProductIdAndStatus(productId, status);
        return ResponseEntity.ok(variations);
    }

    /**
     * GET /woo/products/{productId}/variations/stock/{stockStatus} - Get variations by stock status for a product
     */
    @GetMapping(value = "/woo/products/{productId}/variations/stock/{stockStatus}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<WooVariation>> getVariationsByProductIdAndStockStatus(
            @PathVariable Long productId,
            @PathVariable String stockStatus) {
        logger.info("GET /woo/products/{}/variations/stock/{} - Fetching variations", productId, stockStatus);
        List<WooVariation> variations = variationService.getVariationsByProductIdAndStockStatus(productId, stockStatus);
        return ResponseEntity.ok(variations);
    }

    /**
     * PUT /woo/products/{productId}/variations/{id} - Update a variation for a product
     */
    @PutMapping(value = "/woo/products/{productId}/variations/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WooVariation> updateVariationForProduct(
            @PathVariable Long productId,
            @PathVariable Long id,
            @RequestBody WooVariation variation) {
        logger.info("PUT /woo/products/{}/variations/{} - Updating variation", productId, id);
        variation.setProductId(productId);
        WooVariation updated = variationService.updateVariation(id, variation);
        return ResponseEntity.ok(updated);
    }

    /**
     * PATCH /woo/products/{productId}/variations/{id} - Patch a variation for a product
     */
    @PatchMapping(value = "/woo/products/{productId}/variations/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WooVariation> patchVariationForProduct(
            @PathVariable Long productId,
            @PathVariable Long id,
            @RequestBody WooVariation variation) {
        logger.info("PATCH /woo/products/{}/variations/{} - Patching variation", productId, id);
        WooVariation patched = variationService.patchVariation(id, variation);
        return ResponseEntity.ok(patched);
    }

    /**
     * DELETE /woo/products/{productId}/variations/{id} - Delete a variation for a product
     */
    @DeleteMapping("/woo/products/{productId}/variations/{id}")
    public ResponseEntity<Void> deleteVariationForProduct(
            @PathVariable Long productId,
            @PathVariable Long id) {
        logger.info("DELETE /woo/products/{}/variations/{} - Deleting variation", productId, id);
        variationService.deleteVariation(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * DELETE /woo/products/{productId}/variations - Delete all variations for a product
     */
    @DeleteMapping("/woo/products/{productId}/variations")
    public ResponseEntity<Void> deleteAllVariationsForProduct(@PathVariable Long productId) {
        logger.info("DELETE /woo/products/{}/variations - Deleting all variations for product", productId);
        variationService.deleteVariationsByProductId(productId);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /woo/products/{productId}/variations/count - Get variation count for a product
     */
    @GetMapping(value = "/woo/products/{productId}/variations/count", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Long>> getVariationCountForProduct(@PathVariable Long productId) {
        logger.info("GET /woo/products/{}/variations/count - Fetching count", productId);
        Map<String, Long> count = new HashMap<>();
        count.put("count", variationService.countVariationsByProductId(productId));
        return ResponseEntity.ok(count);
    }
}
