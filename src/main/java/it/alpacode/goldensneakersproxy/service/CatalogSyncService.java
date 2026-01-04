package it.alpacode.goldensneakersproxy.service;

import it.alpacode.goldensneakersproxy.client.woocommerce.WooCommerceClient;
import it.alpacode.goldensneakersproxy.client.woocommerce.dto.*;
import it.alpacode.goldensneakersproxy.config.ShopProperties;
import it.alpacode.goldensneakersproxy.exception.CatalogSyncException;
import it.alpacode.goldensneakersproxy.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Main orchestrator for catalog synchronization.
 * Coordinates the bulk sync process with transactional support.
 */
@Service
public class CatalogSyncService {

    private static final Logger log = LoggerFactory.getLogger(CatalogSyncService.class);

    private final WooCommerceClient wooClient;
    private final TaxonomyResolverService taxonomyResolver;
    private final ProductDiffService diffService;
    private final SyncTransactionManager transactionManager;
    private final ShopProperties shopProperties;

    public CatalogSyncService(
            WooCommerceClient wooClient,
            TaxonomyResolverService taxonomyResolver,
            ProductDiffService diffService,
            SyncTransactionManager transactionManager,
            ShopProperties shopProperties) {
        this.wooClient = wooClient;
        this.taxonomyResolver = taxonomyResolver;
        this.diffService = diffService;
        this.transactionManager = transactionManager;
        this.shopProperties = shopProperties;
    }

    /**
     * Synchronize the entire catalog.
     * - Creates new products
     * - Updates existing products
     * - Marks missing products as out of stock
     *
     * Transactional: automatic rollback on error.
     */
    public SyncResult syncCatalog(List<CatalogProduct> feedProducts) {
        log.info("Starting catalog sync with {} products from feed", feedProducts.size());

        long startTime = System.currentTimeMillis();
        SyncResult result = SyncResult.success();
        String transactionId = transactionManager.begin();

        try {
            // PHASE 1: TAXONOMY RESOLUTION
            log.info("[Phase 1/6] Resolving taxonomies...");
            TaxonomyResolutionResult taxonomies = taxonomyResolver.resolveAll(feedProducts);
            result.setTaxonomiesCreated(taxonomies.getCreatedCount());
            log.info("Taxonomies resolved - {} created", taxonomies.getCreatedCount());

            // PHASE 2: FETCH EXISTING PRODUCTS
            log.info("[Phase 2/6] Fetching existing products from shop...");
            Map<String, ProductDto> existingProducts = wooClient.fetchAllProductsBySku();
            log.info("Fetched {} existing products", existingProducts.size());

            // PHASE 3: DIFF CALCULATION
            log.info("[Phase 3/6] Calculating diff...");
            CatalogDiff diff = diffService.calculateDiff(feedProducts, existingProducts);

            log.info("Diff summary - Create: {}, Update: {}, MarkOutOfStock: {}",
                    diff.getToCreate().size(),
                    diff.getToUpdate().size(),
                    diff.getToMarkOutOfStock().size());

            // PHASE 4: BATCH CREATE
            if (!diff.getToCreate().isEmpty()) {
                log.info("[Phase 4/6] Creating {} new products...", diff.getToCreate().size());
                List<ProductCreateRequestDto> createRequests = buildProductCreateRequests(
                        diff.getToCreate(), taxonomies);
                List<ProductDto> created = wooClient.createProductsBatch(createRequests);
                result.addCreated(created);
                transactionManager.trackCreated(transactionId, created);
                log.info("Created {} products", created.size());
            } else {
                log.info("[Phase 4/6] No products to create");
            }

            // PHASE 5: BATCH UPDATE
            if (!diff.getToUpdate().isEmpty()) {
                log.info("[Phase 5/6] Updating {} products...", diff.getToUpdate().size());
                List<ProductUpdateRequestDto> updateRequests = buildProductUpdateRequests(
                        diff.getToUpdate(), taxonomies, existingProducts);
                List<ProductDto> updated = wooClient.updateProductsBatch(updateRequests);
                result.addUpdated(updated);
                log.info("Updated {} products", updated.size());
            } else {
                log.info("[Phase 5/6] No products to update");
            }

            // PHASE 6: MARK OUT OF STOCK
            if (shopProperties.getSync().isMarkMissingOutOfStock() && !diff.getToMarkOutOfStock().isEmpty()) {
                log.info("[Phase 6/6] Marking {} products out of stock...",
                        diff.getToMarkOutOfStock().size());
                wooClient.markOutOfStockBatch(diff.getToMarkOutOfStock());
                result.addMarkedOutOfStock(diff.getToMarkOutOfStock().size());
                log.info("Marked {} products out of stock", diff.getToMarkOutOfStock().size());
            } else {
                log.info("[Phase 6/6] No products to mark out of stock");
            }

            // PHASE 7: CREATE VARIATIONS IN PARALLEL
            log.info("Creating variations...");
            List<ProductDto> allProducts = new ArrayList<>();
            allProducts.addAll(result.getCreated());
            allProducts.addAll(result.getUpdated());

            Map<Long, List<VariationDto>> variations = createVariationsParallel(
                    allProducts, feedProducts);
            result.setVariations(variations);
            log.info("Created/updated {} total variations", result.getTotalVariationsCount());

            // Commit transaction
            transactionManager.commit(transactionId);

            result.setDurationMs(System.currentTimeMillis() - startTime);
            result.setStatus("SUCCESS");

            log.info("Sync completed successfully in {}ms: {} created, {} updated, {} marked out of stock, {} variations",
                    result.getDurationMs(),
                    result.getCreatedCount(),
                    result.getUpdatedCount(),
                    result.getMarkedOutOfStockCount(),
                    result.getTotalVariationsCount());

            return result;

        } catch (Exception e) {
            log.error("Sync failed, rolling back transaction {}", transactionId, e);

            try {
                transactionManager.rollback(transactionId);
            } catch (Exception rollbackError) {
                log.error("Rollback also failed", rollbackError);
            }

            result.setDurationMs(System.currentTimeMillis() - startTime);
            result.setStatus("FAILED");
            result.setErrorMessage(e.getMessage());

            throw new CatalogSyncException("Catalog sync failed and was rolled back", e, true);
        }
    }

    /**
     * Create variations for all products in parallel.
     */
    private Map<Long, List<VariationDto>> createVariationsParallel(
            List<ProductDto> products,
            List<CatalogProduct> feedProducts) {

        if (!shopProperties.getSync().isParallelVariations()) {
            return createVariationsSequential(products, feedProducts);
        }

        Map<String, CatalogProduct> feedMap = feedProducts.stream()
                .collect(Collectors.toMap(CatalogProduct::getSku, p -> p, (a, b) -> a));

        Map<Long, List<VariationDto>> results = new ConcurrentHashMap<>();
        int maxThreads = shopProperties.getSync().getMaxThreads();

        ExecutorService executor = Executors.newFixedThreadPool(maxThreads);
        List<Future<?>> futures = new ArrayList<>();

        for (ProductDto product : products) {
            if (product.getSku() == null) continue;

            CatalogProduct feedProduct = feedMap.get(product.getSku());
            if (feedProduct == null || feedProduct.getVariations().isEmpty()) continue;

            futures.add(executor.submit(() -> {
                try {
                    List<VariationDto> variations = wooClient.upsertVariations(
                            product.getId(),
                            feedProduct.getVariations());
                    results.put(product.getId(), variations);
                } catch (Exception e) {
                    log.error("Failed to create variations for product {} ({})",
                            product.getId(), product.getSku(), e);
                    throw new RuntimeException("Variation creation failed for product " + product.getId(), e);
                }
            }));
        }

        // Wait for all variations to complete
        for (Future<?> future : futures) {
            try {
                future.get(60, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Variation creation interrupted", e);
            } catch (ExecutionException e) {
                throw new RuntimeException("Variation creation failed", e.getCause());
            } catch (TimeoutException e) {
                throw new RuntimeException("Variation creation timed out", e);
            }
        }

        executor.shutdown();

        return results;
    }

    /**
     * Create variations sequentially (fallback).
     */
    private Map<Long, List<VariationDto>> createVariationsSequential(
            List<ProductDto> products,
            List<CatalogProduct> feedProducts) {

        Map<String, CatalogProduct> feedMap = feedProducts.stream()
                .collect(Collectors.toMap(CatalogProduct::getSku, p -> p, (a, b) -> a));

        Map<Long, List<VariationDto>> results = new HashMap<>();

        for (ProductDto product : products) {
            if (product.getSku() == null) continue;

            CatalogProduct feedProduct = feedMap.get(product.getSku());
            if (feedProduct == null || feedProduct.getVariations().isEmpty()) continue;

            try {
                List<VariationDto> variations = wooClient.upsertVariations(
                        product.getId(),
                        feedProduct.getVariations());
                results.put(product.getId(), variations);
            } catch (Exception e) {
                log.error("Failed to create variations for product {} ({})",
                        product.getId(), product.getSku(), e);
                throw new RuntimeException("Variation creation failed", e);
            }
        }

        return results;
    }

    /**
     * Build product create requests from catalog products.
     */
    private List<ProductCreateRequestDto> buildProductCreateRequests(
            List<CatalogProduct> products,
            TaxonomyResolutionResult taxonomies) {

        return products.stream()
                .map(p -> {
                    ProductCreateRequestDto dto = new ProductCreateRequestDto();
                    dto.setName(p.getName());
                    dto.setSku(p.getSku());
                    dto.setType(p.getType());
                    dto.setStatus(p.getStatus());
                    dto.setDescription(p.getDescription());
                    dto.setShortDescription(p.getShortDescription());
                    dto.setWeight(p.getWeight());
                    dto.setDimensions(p.getDimensions());

                    // Resolve taxonomies
                    dto.setBrands(taxonomies.resolveBrands(p.getBrands()));
                    dto.setTags(buildTagDtos(taxonomies.resolveTags(p.getTags())));
                    dto.setCategories(buildCategoryDtos(taxonomies.resolveCategories(p.getCategories())));

                    // Set images
                    dto.setImages(p.getImages());

                    // Build attributes from variations (for variable products)
                    if ("variable".equals(p.getType()) && !p.getVariations().isEmpty()) {
                        List<String> sizes = p.getAllSizes();
                        AttributeDto sizeAttr = new AttributeDto("Taglie", sizes);
                        sizeAttr.setVisible(true);
                        sizeAttr.setVariation(true);
                        dto.setAttributes(List.of(sizeAttr));
                    } else if (!p.getAttributes().isEmpty()) {
                        dto.setAttributes(p.getAttributes());
                    }

                    dto.setMetaData(p.getMetaData());

                    return dto;
                })
                .toList();
    }

    /**
     * Build product update requests from catalog products.
     */
    private List<ProductUpdateRequestDto> buildProductUpdateRequests(
            List<CatalogProduct> products,
            TaxonomyResolutionResult taxonomies,
            Map<String, ProductDto> existingProducts) {

        return products.stream()
                .map(p -> {
                    ProductDto existing = existingProducts.get(p.getSku());
                    if (existing == null) return null;

                    ProductUpdateRequestDto dto = new ProductUpdateRequestDto();
                    dto.setId(existing.getId());
                    dto.setName(p.getName());
                    dto.setDescription(p.getDescription());
                    dto.setShortDescription(p.getShortDescription());

                    // Resolve taxonomies
                    dto.setBrands(taxonomies.resolveBrands(p.getBrands()));
                    dto.setTags(buildTagDtos(taxonomies.resolveTags(p.getTags())));

                    // Set images
                    dto.setImages(p.getImages());
                    dto.setMetaData(p.getMetaData());

                    // Set stock status back to instock
                    dto.setStockStatus("instock");

                    return dto;
                })
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Build TagDto list from resolved IDs.
     */
    private List<TagDto> buildTagDtos(List<Long> ids) {
        return ids.stream()
                .map(TagDto::new)
                .toList();
    }

    /**
     * Build CategoryDto list from resolved IDs.
     */
    private List<CategoryDto> buildCategoryDtos(List<Long> ids) {
        return ids.stream()
                .map(CategoryDto::new)
                .toList();
    }
}
