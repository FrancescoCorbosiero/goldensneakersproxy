package it.alpacode.goldensneakersproxy.controller;

import it.alpacode.goldensneakersproxy.entity.woocommerce.WooProduct;
import it.alpacode.goldensneakersproxy.service.WooProductService;
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
 * REST Controller for WooCommerce Product CRUD operations.
 * Base path: /woo/products
 */
@RestController
@RequestMapping("/woo/products")
public class WooProductController {

    private static final Logger logger = LoggerFactory.getLogger(WooProductController.class);

    private final WooProductService productService;

    public WooProductController(WooProductService productService) {
        this.productService = productService;
    }

    // ========== CREATE ==========

    /**
     * POST /woo/products - Create a new product
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WooProduct> createProduct(@RequestBody WooProduct product) {
        logger.info("POST /woo/products - Creating new product: {}", product.getName());
        WooProduct created = productService.createProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ========== READ ==========

    /**
     * GET /woo/products - List all products with optional pagination
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllProducts(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDir) {

        logger.info("GET /woo/products - Fetching products (page={}, size={}, sortBy={}, sortDir={})",
                page, size, sortBy, sortDir);

        if (page != null && size != null) {
            Sort sort = sortDir.equalsIgnoreCase("desc") ?
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<WooProduct> productPage = productService.getAllProducts(pageable);
            return ResponseEntity.ok(productPage);
        }

        List<WooProduct> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    /**
     * GET /woo/products/{id} - Get product by ID
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WooProduct> getProductById(@PathVariable Long id) {
        logger.info("GET /woo/products/{} - Fetching product", id);
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /woo/products/sku/{sku} - Get product by SKU
     */
    @GetMapping(value = "/sku/{sku}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WooProduct> getProductBySku(@PathVariable String sku) {
        logger.info("GET /woo/products/sku/{} - Fetching product", sku);
        return productService.getProductBySku(sku)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /woo/products/slug/{slug} - Get product by slug
     */
    @GetMapping(value = "/slug/{slug}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WooProduct> getProductBySlug(@PathVariable String slug) {
        logger.info("GET /woo/products/slug/{} - Fetching product", slug);
        return productService.getProductBySlug(slug)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /woo/products/type/{type} - Get products by type
     */
    @GetMapping(value = "/type/{type}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<WooProduct>> getProductsByType(@PathVariable String type) {
        logger.info("GET /woo/products/type/{} - Fetching products", type);
        List<WooProduct> products = productService.getProductsByType(type);
        return ResponseEntity.ok(products);
    }

    /**
     * GET /woo/products/status/{status} - Get products by status
     */
    @GetMapping(value = "/status/{status}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getProductsByStatus(
            @PathVariable String status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {

        logger.info("GET /woo/products/status/{} - Fetching products (page={}, size={})", status, page, size);

        if (page != null && size != null) {
            Pageable pageable = PageRequest.of(page, size);
            Page<WooProduct> productPage = productService.getProductsByStatus(status, pageable);
            return ResponseEntity.ok(productPage);
        }

        List<WooProduct> products = productService.getProductsByStatus(status);
        return ResponseEntity.ok(products);
    }

    /**
     * GET /woo/products/featured - Get featured products
     */
    @GetMapping(value = "/featured", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<WooProduct>> getFeaturedProducts() {
        logger.info("GET /woo/products/featured - Fetching featured products");
        List<WooProduct> products = productService.getFeaturedProducts();
        return ResponseEntity.ok(products);
    }

    /**
     * GET /woo/products/search - Search products by keyword
     */
    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> searchProducts(
            @RequestParam String q,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {

        logger.info("GET /woo/products/search?q={} - Searching products (page={}, size={})", q, page, size);

        if (page != null && size != null) {
            Pageable pageable = PageRequest.of(page, size);
            Page<WooProduct> productPage = productService.searchProducts(q, pageable);
            return ResponseEntity.ok(productPage);
        }

        List<WooProduct> products = productService.searchProducts(q);
        return ResponseEntity.ok(products);
    }

    /**
     * GET /woo/products/stock/{stockStatus} - Get products by stock status
     */
    @GetMapping(value = "/stock/{stockStatus}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<WooProduct>> getProductsByStockStatus(@PathVariable String stockStatus) {
        logger.info("GET /woo/products/stock/{} - Fetching products", stockStatus);
        List<WooProduct> products = productService.getProductsByStockStatus(stockStatus);
        return ResponseEntity.ok(products);
    }

    /**
     * GET /woo/products/category/{categoryId} - Get products by category ID
     */
    @GetMapping(value = "/category/{categoryId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<WooProduct>> getProductsByCategoryId(@PathVariable Integer categoryId) {
        logger.info("GET /woo/products/category/{} - Fetching products", categoryId);
        List<WooProduct> products = productService.getProductsByCategoryId(categoryId);
        return ResponseEntity.ok(products);
    }

    /**
     * GET /woo/products/tag/{tagId} - Get products by tag ID
     */
    @GetMapping(value = "/tag/{tagId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<WooProduct>> getProductsByTagId(@PathVariable Integer tagId) {
        logger.info("GET /woo/products/tag/{} - Fetching products", tagId);
        List<WooProduct> products = productService.getProductsByTagId(tagId);
        return ResponseEntity.ok(products);
    }

    // ========== UPDATE ==========

    /**
     * PUT /woo/products/{id} - Full update of a product
     */
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WooProduct> updateProduct(
            @PathVariable Long id,
            @RequestBody WooProduct product) {
        logger.info("PUT /woo/products/{} - Updating product", id);
        WooProduct updated = productService.updateProduct(id, product);
        return ResponseEntity.ok(updated);
    }

    /**
     * PATCH /woo/products/{id} - Partial update of a product
     */
    @PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WooProduct> patchProduct(
            @PathVariable Long id,
            @RequestBody WooProduct product) {
        logger.info("PATCH /woo/products/{} - Patching product", id);
        WooProduct patched = productService.patchProduct(id, product);
        return ResponseEntity.ok(patched);
    }

    // ========== DELETE ==========

    /**
     * DELETE /woo/products/{id} - Delete a product
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        logger.info("DELETE /woo/products/{} - Deleting product", id);
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * DELETE /woo/products - Delete all products (use with caution)
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteAllProducts() {
        logger.warn("DELETE /woo/products - Deleting all products");
        productService.deleteAllProducts();
        return ResponseEntity.noContent().build();
    }

    // ========== STATISTICS ==========

    /**
     * GET /woo/products/stats - Get product statistics
     */
    @GetMapping(value = "/stats", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getProductStats() {
        logger.info("GET /woo/products/stats - Fetching product statistics");

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalProducts", productService.countProducts());
        stats.put("productTypes", productService.getAllProductTypes());
        stats.put("statuses", productService.getAllStatuses());

        Map<String, Long> statusCounts = new HashMap<>();
        for (String status : productService.getAllStatuses()) {
            statusCounts.put(status, productService.countProductsByStatus(status));
        }
        stats.put("statusCounts", statusCounts);

        Map<String, Long> typeCounts = new HashMap<>();
        for (String type : productService.getAllProductTypes()) {
            typeCounts.put(type, productService.countProductsByType(type));
        }
        stats.put("typeCounts", typeCounts);

        return ResponseEntity.ok(stats);
    }
}
