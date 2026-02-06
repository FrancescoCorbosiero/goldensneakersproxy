package it.alpacode.goldensneakersproxy.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.alpacode.goldensneakersproxy.config.AssortmentMapperConfig;
import it.alpacode.goldensneakersproxy.dto.GsProduct;
import it.alpacode.goldensneakersproxy.dto.GsSize;
import it.alpacode.goldensneakersproxy.entity.woocommerce.*;
import it.alpacode.goldensneakersproxy.entity.wordpress.WpAttributeLookup;
import it.alpacode.goldensneakersproxy.entity.wordpress.WpBrandLookup;
import it.alpacode.goldensneakersproxy.entity.wordpress.WpCategoryLookup;
import it.alpacode.goldensneakersproxy.entity.wordpress.WpMediaLookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class AssortmentMapperService {

    private static final Logger logger = LoggerFactory.getLogger(AssortmentMapperService.class);

    private final GoldenSneakersClient gsClient;
    private final ObjectMapper objectMapper;
    private final AssortmentMapperConfig config;
    private final WordPressUploadService wpUploadService;
    private final WooProductService productService;
    private final WooVariationService variationService;

    public AssortmentMapperService(GoldenSneakersClient gsClient,
                                    ObjectMapper objectMapper,
                                    AssortmentMapperConfig config,
                                    WordPressUploadService wpUploadService,
                                    WooProductService productService,
                                    WooVariationService variationService) {
        this.gsClient = gsClient;
        this.objectMapper = objectMapper;
        this.config = config;
        this.wpUploadService = wpUploadService;
        this.productService = productService;
        this.variationService = variationService;
    }

    // ========== FULL SYNC ==========

    /**
     * Fetch the full GS assortment, map to WooCommerce products + variations,
     * and upsert into the local database.
     * Validates that required taxonomy lookups exist before starting.
     */
    public SyncResult syncFullAssortment() {
        logger.info("Starting full assortment sync");

        // Fail-fast: validate required taxonomies exist before processing anything
        validateTaxonomyLookups();

        List<GsProduct> gsProducts = fetchAssortment();
        logger.info("Fetched {} products from Golden Sneakers", gsProducts.size());

        int created = 0;
        int updated = 0;
        int skipped = 0;
        int failed = 0;
        List<String> errors = new ArrayList<>();

        for (GsProduct gsProduct : gsProducts) {
            try {
                validateGsProduct(gsProduct);
                SyncAction action = syncSingleProduct(gsProduct);
                if (action == SyncAction.CREATED) created++;
                else if (action == SyncAction.UPDATED) updated++;
            } catch (ProductSkippedException e) {
                skipped++;
                logger.warn("Skipped product: {}", e.getMessage());
            } catch (Exception e) {
                failed++;
                String error = "Failed to sync product " + gsProduct.getSku() + ": " + e.getMessage();
                errors.add(error);
                logger.error(error, e);
            }
        }

        logger.info("Assortment sync complete: {} created, {} updated, {} skipped, {} failed",
            created, updated, skipped, failed);

        return new SyncResult(gsProducts.size(), created, updated, skipped, failed, errors);
    }

    /**
     * Sync a single GS product by its GS product ID.
     */
    public SyncResult syncProductById(Integer gsProductId) {
        logger.info("Syncing product with GS ID: {}", gsProductId);

        validateTaxonomyLookups();

        String response = gsClient.fetchAssortmentById(String.valueOf(gsProductId), Map.of()).block();
        GsProduct gsProduct;
        try {
            gsProduct = objectMapper.readValue(response, GsProduct.class);
        } catch (Exception e) {
            throw new AssortmentSyncException("Failed to parse GS product " + gsProductId, e);
        }

        validateGsProduct(gsProduct);

        List<String> errors = new ArrayList<>();
        int created = 0;
        int updated = 0;

        try {
            SyncAction action = syncSingleProduct(gsProduct);
            if (action == SyncAction.CREATED) created = 1;
            else if (action == SyncAction.UPDATED) updated = 1;
        } catch (Exception e) {
            errors.add("Failed to sync product " + gsProduct.getSku() + ": " + e.getMessage());
            logger.error("Failed to sync product {}: {}", gsProduct.getSku(), e.getMessage(), e);
        }

        return new SyncResult(1, created, updated, 0, errors.isEmpty() ? 0 : 1, errors);
    }

    // ========== PREVIEW (DRY RUN) ==========

    /**
     * Preview the mapping of a single GS product without saving.
     */
    public MappedProduct previewProductById(Integer gsProductId) {
        logger.info("Previewing mapping for GS product ID: {}", gsProductId);

        String response = gsClient.fetchAssortmentById(String.valueOf(gsProductId), Map.of()).block();
        GsProduct gsProduct;
        try {
            gsProduct = objectMapper.readValue(response, GsProduct.class);
        } catch (Exception e) {
            throw new AssortmentSyncException("Failed to parse GS product " + gsProductId, e);
        }

        return mapProductWithVariations(gsProduct);
    }

    /**
     * Preview the mapping of the full assortment without saving.
     */
    public List<MappedProduct> previewFullAssortment() {
        logger.info("Previewing mapping for full assortment");

        List<GsProduct> gsProducts = fetchAssortment();
        return gsProducts.stream()
            .map(this::mapProductWithVariations)
            .collect(Collectors.toList());
    }

    // ========== VALIDATION ==========

    /**
     * Validate that required taxonomy lookups are populated.
     * Fails fast before any product processing begins.
     */
    private void validateTaxonomyLookups() {
        List<String> missing = new ArrayList<>();

        if (wpUploadService.getCategoryByName(config.getDefaultCategory()).isEmpty()) {
            missing.add("Category '" + config.getDefaultCategory() + "'");
        }

        if (wpUploadService.getAttributeByName(config.getSizeAttributeName()).isEmpty()) {
            missing.add("Attribute '" + config.getSizeAttributeName() + "'");
        }

        if (!missing.isEmpty()) {
            throw new AssortmentSyncException(
                "Required taxonomy lookups not found: " + String.join(", ", missing)
                    + ". Run POST /wp-upload/taxonomies/sync first.",
                null);
        }
    }

    /**
     * Validate a GS product has the minimum required fields.
     * Skips products without sizes (nothing to create variations from).
     */
    private void validateGsProduct(GsProduct gs) {
        if (gs.getSku() == null || gs.getSku().isBlank()) {
            throw new ProductSkippedException("Product with GS ID " + gs.getId() + " has no SKU");
        }
        if (gs.getName() == null || gs.getName().isBlank()) {
            throw new ProductSkippedException("Product SKU " + gs.getSku() + " has no name");
        }
        if (gs.getSizes() == null || gs.getSizes().isEmpty()) {
            throw new ProductSkippedException("Product SKU " + gs.getSku() + " has no sizes");
        }

        // Validate at least one size has a valid price
        boolean hasPrice = gs.getSizes().stream()
            .anyMatch(s -> s.getOfferPrice() != null && s.getOfferPrice().compareTo(BigDecimal.ZERO) > 0);
        if (!hasPrice) {
            throw new ProductSkippedException("Product SKU " + gs.getSku() + " has no valid prices");
        }

        // Validate brand exists in lookup (warn but don't skip - brand is not strictly required)
        if (gs.getBrandName() != null && wpUploadService.getBrandByName(gs.getBrandName()).isEmpty()) {
            logger.warn("Brand '{}' not found in lookup for product {}. Upload it via POST /wp-upload/brands first.",
                gs.getBrandName(), gs.getSku());
        }
    }

    // ========== INTERNAL SYNC LOGIC ==========

    private SyncAction syncSingleProduct(GsProduct gsProduct) {
        MappedProduct mapped = mapProductWithVariations(gsProduct);

        // Check if product already exists by SKU
        Optional<WooProduct> existing = productService.getProductBySku(gsProduct.getSku());

        WooProduct savedProduct;
        SyncAction action;

        if (existing.isPresent()) {
            // Update existing product
            savedProduct = productService.updateProduct(existing.get().getId(), mapped.product());
            action = SyncAction.UPDATED;
            logger.info("Updated product SKU: {} (ID: {})", gsProduct.getSku(), savedProduct.getId());

            // Delete old variations for this product and recreate
            variationService.deleteVariationsByProductId(savedProduct.getId());
        } else {
            // Create new product
            savedProduct = productService.createProduct(mapped.product());
            action = SyncAction.CREATED;
            logger.info("Created product SKU: {} (ID: {})", gsProduct.getSku(), savedProduct.getId());
        }

        // Create variations
        List<Integer> variationIds = new ArrayList<>();
        for (WooVariation variation : mapped.variations()) {
            variation.setProductId(savedProduct.getId());
            WooVariation savedVariation = variationService.createVariation(variation);
            variationIds.add(savedVariation.getId().intValue());
        }

        // Update product's variation ID list
        if (!variationIds.isEmpty()) {
            savedProduct.setVariations(variationIds);
            productService.updateProduct(savedProduct.getId(), savedProduct);
        }

        return action;
    }

    // ========== MAPPING LOGIC ==========

    private MappedProduct mapProductWithVariations(GsProduct gs) {
        WooProduct product = mapProduct(gs);
        List<WooVariation> variations = gs.getSizes().stream()
            .map(size -> mapVariation(size, gs.getSku()))
            .collect(Collectors.toList());
        return new MappedProduct(product, variations);
    }

    /**
     * Map a GS product to a WooCommerce product entity.
     * Resolves taxonomy IDs from lookup tables.
     * Uploads media on-demand if not already in lookup.
     */
    private WooProduct mapProduct(GsProduct gs) {
        WooProduct product = new WooProduct();

        // Core fields
        product.setName(gs.getName());
        product.setSku(gs.getSku());
        product.setSlug(slugify(gs.getName()));
        boolean hasSizes = gs.getSizes() != null && !gs.getSizes().isEmpty();
        product.setType(hasSizes ? "variable" : "simple");
        product.setStatus("publish");
        product.setCatalogVisibility("visible");
        product.setFeatured(false);

        // Stock
        product.setManageStock(true);
        int totalStock = hasSizes
            ? gs.getSizes().stream()
                .mapToInt(s -> s.getAvailableQuantity() != null ? s.getAvailableQuantity() : 0)
                .sum()
            : 0;
        product.setStockQuantity(totalStock);
        product.setStockStatus(totalStock > 0 ? "instock" : "outofstock");

        // Price (min offer_price with markup across sizes that have a valid price)
        if (hasSizes) {
            BigDecimal minPrice = gs.getSizes().stream()
                .map(GsSize::getOfferPrice)
                .filter(p -> p != null && p.compareTo(BigDecimal.ZERO) > 0)
                .min(BigDecimal::compareTo)
                .orElse(null);
            if (minPrice != null) {
                product.setRegularPrice(applyMarkup(minPrice).toPlainString());
            }
        }

        // Category (resolve from lookup - guaranteed to exist by validateTaxonomyLookups)
        List<ProductCategory> categories = new ArrayList<>();
        WpCategoryLookup categoryLookup = wpUploadService.getCategoryByName(config.getDefaultCategory()).get();
        ProductCategory cat = new ProductCategory();
        cat.setId(categoryLookup.getWordpressId());
        cat.setName(categoryLookup.getName());
        cat.setSlug(categoryLookup.getSlug());
        categories.add(cat);
        product.setCategories(categories);

        // Brand as tag (resolve from lookup)
        List<ProductTag> tags = new ArrayList<>();
        if (gs.getBrandName() != null) {
            Optional<WpBrandLookup> brandLookup = wpUploadService.getBrandByName(gs.getBrandName());
            if (brandLookup.isPresent()) {
                ProductTag tag = new ProductTag();
                tag.setId(brandLookup.get().getWordpressId());
                tag.setName(brandLookup.get().getName());
                tag.setSlug(brandLookup.get().getSlug());
                tags.add(tag);
            }
        }
        product.setTags(tags);

        // Image (upload on-demand, resolve from lookup)
        List<ProductImage> images = new ArrayList<>();
        if (gs.getImageFullUrl() != null && !gs.getImageFullUrl().isBlank()) {
            String imageUrl = normalizeImageUrl(gs.getImageFullUrl(), gs.getSku());
            WpMediaLookup mediaLookup = resolveMedia(imageUrl);
            if (mediaLookup != null) {
                ProductImage img = new ProductImage();
                img.setId(mediaLookup.getWordpressId());
                img.setSrc(mediaLookup.getUploadedUrl());
                img.setName(gs.getName());
                img.setAlt(gs.getName());
                images.add(img);
            }
        }
        product.setImages(images);

        // Size attribute (resolve from lookup - guaranteed to exist by validateTaxonomyLookups)
        if (hasSizes) {
            List<ProductAttribute> attributes = new ArrayList<>();
            ProductAttribute sizeAttr = new ProductAttribute();
            WpAttributeLookup attrLookup = wpUploadService.getAttributeByName(config.getSizeAttributeName()).get();
            sizeAttr.setId(attrLookup.getWordpressId());
            sizeAttr.setName(config.getSizeAttributeName());
            sizeAttr.setPosition(0);
            sizeAttr.setVisible(true);
            sizeAttr.setVariation(true);
            List<String> sizeOptions = gs.getSizes().stream()
                .map(s -> formatSizeOption(s.getSizeEu(), s.getSizeUs()))
                .collect(Collectors.toList());
            sizeAttr.setOptions(sizeOptions);
            attributes.add(sizeAttr);
            product.setAttributes(attributes);
        }

        // Metadata (store GS source info)
        List<ProductMetaData> metaData = new ArrayList<>();
        metaData.add(createProductMeta("_gs_product_id", String.valueOf(gs.getId())));
        if (gs.getSizeMapperName() != null) {
            metaData.add(createProductMeta("_gs_size_mapper", gs.getSizeMapperName()));
        }
        product.setMetaData(metaData);

        return product;
    }

    /**
     * Map a GS size to a WooCommerce variation entity.
     */
    private WooVariation mapVariation(GsSize size, String parentSku) {
        WooVariation variation = new WooVariation();

        // SKU: parent-EU{size}
        variation.setSku(parentSku + "-EU" + size.getSizeEu());
        variation.setStatus("publish");

        // Price (offer_price with markup)
        if (size.getOfferPrice() != null && size.getOfferPrice().compareTo(BigDecimal.ZERO) > 0) {
            variation.setRegularPrice(applyMarkup(size.getOfferPrice()).toPlainString());
        }

        // Stock
        variation.setManageStock(true);
        int qty = size.getAvailableQuantity() != null ? size.getAvailableQuantity() : 0;
        variation.setStockQuantity(qty);
        variation.setStockStatus(qty > 0 ? "instock" : "outofstock");

        // Barcode as global unique ID
        if (size.getBarcode() != null && !size.getBarcode().isBlank()) {
            variation.setGlobalUniqueId(size.getBarcode());
        }

        // Size attribute (guaranteed to exist by validateTaxonomyLookups)
        List<VariationAttribute> attributes = new ArrayList<>();
        VariationAttribute sizeAttr = new VariationAttribute();
        WpAttributeLookup attrLookup = wpUploadService.getAttributeByName(config.getSizeAttributeName()).get();
        sizeAttr.setId(attrLookup.getWordpressId());
        sizeAttr.setName(config.getSizeAttributeName());
        sizeAttr.setOption(formatSizeOption(size.getSizeEu(), size.getSizeUs()));
        attributes.add(sizeAttr);
        variation.setAttributes(attributes);

        // Metadata
        List<VariationMetaData> metaData = new ArrayList<>();
        metaData.add(createVariationMeta("_gs_size_id", String.valueOf(size.getId())));
        if (size.getOfferPrice() != null) {
            metaData.add(createVariationMeta("_gs_offer_price", size.getOfferPrice().toPlainString()));
        }
        if (size.getPresentedPrice() != null) {
            metaData.add(createVariationMeta("_gs_presented_price", size.getPresentedPrice().toPlainString()));
        }
        variation.setMetaData(metaData);

        return variation;
    }

    // ========== HELPERS ==========

    private List<GsProduct> fetchAssortment() {
        String response = gsClient.fetchAssortment(Map.of()).block();
        try {
            return objectMapper.readValue(response, new TypeReference<List<GsProduct>>() {});
        } catch (Exception e) {
            throw new AssortmentSyncException("Failed to parse GS assortment response", e);
        }
    }

    /**
     * Normalize GS image URL. The GS API returns directory-like URLs ending with /main/.
     * We append a filename derived from the SKU to form a downloadable image URL.
     */
    private String normalizeImageUrl(String imageFullUrl, String sku) {
        // GS URLs look like: https://www.goldensneakers.net/images/DD1873-102/main/
        // If URL ends with /, it's a directory path - append SKU-based filename
        if (imageFullUrl.endsWith("/")) {
            return imageFullUrl + sku + ".jpg";
        }
        return imageFullUrl;
    }

    private WpMediaLookup resolveMedia(String imageUrl) {
        Optional<WpMediaLookup> existing = wpUploadService.getMediaBySourceUrl(imageUrl);
        if (existing.isPresent()) {
            return existing.get();
        }

        // Upload on-demand
        try {
            List<WpMediaLookup> uploaded = wpUploadService.uploadMedia(List.of(imageUrl));
            if (!uploaded.isEmpty()) {
                return uploaded.get(0);
            }
        } catch (Exception e) {
            logger.error("Failed to upload media '{}': {}. Product will be created without image.", imageUrl, e.getMessage());
        }
        return null;
    }

    private BigDecimal applyMarkup(BigDecimal price) {
        BigDecimal multiplier = BigDecimal.ONE.add(
            BigDecimal.valueOf(config.getMarkupPercentage())
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
        );
        return price.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
    }

    private String formatSizeOption(String sizeEu, String sizeUs) {
        if (sizeEu != null && sizeUs != null) {
            return "EU " + sizeEu + " / US " + sizeUs;
        } else if (sizeEu != null) {
            return "EU " + sizeEu;
        } else if (sizeUs != null) {
            return "US " + sizeUs;
        }
        return "N/A";
    }

    private String slugify(String name) {
        if (name == null) return null;
        return name.toLowerCase()
            .replaceAll("[^a-z0-9\\s-]", "")
            .replaceAll("\\s+", "-")
            .replaceAll("-+", "-")
            .replaceAll("^-|-$", "");
    }

    private ProductMetaData createProductMeta(String key, String value) {
        ProductMetaData meta = new ProductMetaData();
        meta.setKey(key);
        meta.setValue(value);
        return meta;
    }

    private VariationMetaData createVariationMeta(String key, String value) {
        VariationMetaData meta = new VariationMetaData();
        meta.setKey(key);
        meta.setValue(value);
        return meta;
    }

    // ========== RESULT TYPES ==========

    public record SyncResult(
        int totalProcessed,
        int created,
        int updated,
        int skipped,
        int failed,
        List<String> errors
    ) {}

    public record MappedProduct(
        WooProduct product,
        List<WooVariation> variations
    ) {}

    private enum SyncAction {
        CREATED, UPDATED
    }

    // ========== EXCEPTIONS ==========

    public static class AssortmentSyncException extends RuntimeException {
        public AssortmentSyncException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class ProductSkippedException extends RuntimeException {
        public ProductSkippedException(String message) {
            super(message);
        }
    }
}
