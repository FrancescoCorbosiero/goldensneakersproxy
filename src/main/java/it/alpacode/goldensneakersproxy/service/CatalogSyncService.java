package it.alpacode.goldensneakersproxy.service;

import it.alpacode.goldensneakersproxy.client.woocommerce.WooCommerceClient;
import it.alpacode.goldensneakersproxy.client.woocommerce.dto.*;
import it.alpacode.goldensneakersproxy.exception.CatalogSyncException;
import it.alpacode.goldensneakersproxy.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Catalog sync service - simple and idempotent.
 *
 * Flow: fetch existing → diff → batch create/update → variations
 *
 * No transaction rollback (idempotent - can re-run safely).
 * No auto taxonomy creation (caller provides IDs).
 * Sequential variation processing (predictable).
 */
@Service
public class CatalogSyncService {

    private static final Logger log = LoggerFactory.getLogger(CatalogSyncService.class);

    private final WooCommerceClient wooClient;
    private final ProductDiffService diffService;

    public CatalogSyncService(WooCommerceClient wooClient, ProductDiffService diffService) {
        this.wooClient = wooClient;
        this.diffService = diffService;
    }

    /**
     * Sync products: creates new, updates existing.
     * Idempotent - safe to re-run.
     */
    public SyncResult syncCatalog(List<CatalogProduct> feedProducts) {
        log.info("Starting catalog sync with {} products", feedProducts.size());
        long startTime = System.currentTimeMillis();
        SyncResult result = new SyncResult();

        try {
            // 1. Fetch existing products
            log.info("[1/4] Fetching existing products...");
            Map<String, ProductDto> existing = wooClient.fetchAllProductsBySku();
            log.info("Found {} existing products", existing.size());

            // 2. Calculate diff
            log.info("[2/4] Calculating diff...");
            CatalogDiff diff = diffService.calculateDiff(feedProducts, existing);
            log.info("Diff: {} to create, {} to update",
                    diff.getToCreate().size(), diff.getToUpdate().size());

            // 3. Batch create new products
            if (!diff.getToCreate().isEmpty()) {
                log.info("[3/4] Creating {} products...", diff.getToCreate().size());
                List<ProductCreateRequestDto> requests = buildCreateRequests(diff.getToCreate());
                List<ProductDto> created = wooClient.createProductsBatch(requests);
                result.addCreated(created);

                // Add created to existing map for variation processing
                for (ProductDto p : created) {
                    if (p.getSku() != null) {
                        existing.put(p.getSku(), p);
                    }
                }
            } else {
                log.info("[3/4] No products to create");
            }

            // 4. Batch update existing products
            if (!diff.getToUpdate().isEmpty()) {
                log.info("[4/4] Updating {} products...", diff.getToUpdate().size());
                List<ProductUpdateRequestDto> requests = buildUpdateRequests(diff.getToUpdate(), existing);
                List<ProductDto> updated = wooClient.updateProductsBatch(requests);
                result.addUpdated(updated);
            } else {
                log.info("[4/4] No products to update");
            }

            // 5. Process variations sequentially
            log.info("Processing variations...");
            Map<Long, List<VariationDto>> variations = processVariations(feedProducts, existing);
            result.setVariations(variations);

            result.setDurationMs(System.currentTimeMillis() - startTime);
            result.setStatus("SUCCESS");

            log.info("Sync complete in {}ms: {} created, {} updated, {} variations",
                    result.getDurationMs(),
                    result.getCreatedCount(),
                    result.getUpdatedCount(),
                    result.getTotalVariationsCount());

            return result;

        } catch (Exception e) {
            log.error("Sync failed: {}", e.getMessage(), e);
            result.setDurationMs(System.currentTimeMillis() - startTime);
            result.setStatus("FAILED");
            result.setErrorMessage(e.getMessage());
            throw new CatalogSyncException("Catalog sync failed: " + e.getMessage(), e);
        }
    }

    /**
     * Mark products as out of stock.
     * Separate operation - caller decides when to use it.
     */
    public int markOutOfStock(List<Long> productIds) {
        if (productIds.isEmpty()) {
            return 0;
        }
        log.info("Marking {} products out of stock", productIds.size());
        wooClient.markOutOfStockBatch(productIds);
        return productIds.size();
    }

    /**
     * Get product IDs that are in shop but not in feed.
     */
    public List<Long> findMissingProducts(List<CatalogProduct> feedProducts) {
        Map<String, ProductDto> existing = wooClient.fetchAllProductsBySku();
        CatalogDiff diff = diffService.calculateDiff(feedProducts, existing);
        return diff.getToMarkOutOfStock();
    }

    private List<ProductCreateRequestDto> buildCreateRequests(List<CatalogProduct> products) {
        return products.stream().map(p -> {
            ProductCreateRequestDto dto = new ProductCreateRequestDto();
            dto.setName(p.getName());
            dto.setSku(p.getSku());
            dto.setType(p.getType());
            dto.setStatus(p.getStatus());
            dto.setDescription(p.getDescription());
            dto.setShortDescription(p.getShortDescription());
            dto.setWeight(p.getWeight());
            dto.setDimensions(p.getDimensions());
            dto.setImages(p.getImages());
            dto.setMetaData(p.getMetaData());

            // Taxonomies - IDs directly
            dto.setBrands(p.getBrandIds());
            dto.setTags(p.getTagIds().stream().map(TagDto::new).toList());
            dto.setCategories(p.getCategoryIds().stream().map(CategoryDto::new).toList());

            // Build size attribute from variations
            if ("variable".equals(p.getType()) && !p.getVariations().isEmpty()) {
                List<String> sizes = p.getAllSizes();
                AttributeDto sizeAttr = new AttributeDto("Taglie", sizes);
                sizeAttr.setVisible(true);
                sizeAttr.setVariation(true);
                dto.setAttributes(List.of(sizeAttr));
            } else if (!p.getAttributes().isEmpty()) {
                dto.setAttributes(p.getAttributes());
            }

            return dto;
        }).toList();
    }

    private List<ProductUpdateRequestDto> buildUpdateRequests(
            List<CatalogProduct> products,
            Map<String, ProductDto> existing) {

        return products.stream().map(p -> {
            ProductDto ex = existing.get(p.getSku());
            if (ex == null) return null;

            ProductUpdateRequestDto dto = new ProductUpdateRequestDto();
            dto.setId(ex.getId());
            dto.setName(p.getName());
            dto.setDescription(p.getDescription());
            dto.setShortDescription(p.getShortDescription());
            dto.setImages(p.getImages());
            dto.setMetaData(p.getMetaData());
            dto.setStockStatus("instock");

            // Taxonomies
            dto.setBrands(p.getBrandIds());
            dto.setTags(p.getTagIds().stream().map(TagDto::new).toList());

            return dto;
        }).filter(Objects::nonNull).toList();
    }

    private Map<Long, List<VariationDto>> processVariations(
            List<CatalogProduct> feedProducts,
            Map<String, ProductDto> existing) {

        Map<Long, List<VariationDto>> results = new HashMap<>();

        for (CatalogProduct feed : feedProducts) {
            if (feed.getVariations().isEmpty()) continue;

            ProductDto product = existing.get(feed.getSku());
            if (product == null) continue;

            try {
                List<VariationDto> variations = wooClient.upsertVariations(
                        product.getId(),
                        feed.getVariations());
                results.put(product.getId(), variations);
                log.debug("Product {} - {} variations", product.getSku(), variations.size());
            } catch (Exception e) {
                log.error("Failed variations for product {}: {}", feed.getSku(), e.getMessage());
                // Continue with other products - don't fail entire sync
            }
        }

        return results;
    }
}
