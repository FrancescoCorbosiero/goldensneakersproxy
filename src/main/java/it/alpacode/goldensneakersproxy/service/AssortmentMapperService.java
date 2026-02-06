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
     */
    public SyncResult syncFullAssortment() {
        logger.info("Starting full assortment sync");

        List<GsProduct> gsProducts = fetchAssortment();
        logger.info("Fetched {} products from Golden Sneakers", gsProducts.size());

        int created = 0;
        int updated = 0;
        int failed = 0;
        List<String> errors = new ArrayList<>();

        for (GsProduct gsProduct : gsProducts) {
            try {
                SyncAction action = syncSingleProduct(gsProduct);
                if (action == SyncAction.CREATED) created++;
                else if (action == SyncAction.UPDATED) updated++;
            } catch (Exception e) {
                failed++;
                String error = "Failed to sync product " + gsProduct.getSku() + ": " + e.getMessage();
                errors.add(error);
                logger.error(error, e);
            }
        }

        logger.info("Assortment sync complete: {} created, {} updated, {} failed",
            created, updated, failed);

        return new SyncResult(gsProducts.size(), created, updated, failed, errors);
    }

    /**
     * Sync a single GS product by its GS product ID.
     */
    public SyncResult syncProductById(Integer gsProductId) {
        logger.info("Syncing product with GS ID: {}", gsProductId);

        String response = gsClient.fetchAssortmentById(String.valueOf(gsProductId), Map.of()).block();
        GsProduct gsProduct;
        try {
            gsProduct = objectMapper.readValue(response, GsProduct.class);
        } catch (Exception e) {
            throw new AssortmentSyncException("Failed to parse GS product " + gsProductId, e);
        }

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

        return new SyncResult(1, created, updated, errors.isEmpty() ? 0 : 1, errors);
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
        product.setType("variable");
        product.setStatus("publish");
        product.setCatalogVisibility("visible");
        product.setFeatured(false);

        // Stock
        product.setManageStock(true);
        int totalStock = gs.getSizes().stream()
            .mapToInt(s -> s.getAvailableQuantity() != null ? s.getAvailableQuantity() : 0)
            .sum();
        product.setStockQuantity(totalStock);
        product.setStockStatus(totalStock > 0 ? "instock" : "outofstock");

        // Price (min presented_price with markup across all sizes)
        BigDecimal minPrice = gs.getSizes().stream()
            .map(GsSize::getOfferPrice)
            .filter(p -> p != null)
            .min(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);
        product.setRegularPrice(applyMarkup(minPrice).toPlainString());

        // Category (resolve from lookup)
        List<ProductCategory> categories = new ArrayList<>();
        Optional<WpCategoryLookup> categoryLookup = wpUploadService.getCategoryByName(config.getDefaultCategory());
        if (categoryLookup.isPresent()) {
            ProductCategory cat = new ProductCategory();
            cat.setId(categoryLookup.get().getWordpressId());
            cat.setName(categoryLookup.get().getName());
            cat.setSlug(categoryLookup.get().getSlug());
            categories.add(cat);
        } else {
            logger.warn("Category '{}' not found in lookup table. Run wp-upload first.", config.getDefaultCategory());
        }
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
            } else {
                logger.warn("Brand '{}' not found in lookup table. Run wp-upload first.", gs.getBrandName());
            }
        }
        product.setTags(tags);

        // Image (upload on-demand, resolve from lookup)
        List<ProductImage> images = new ArrayList<>();
        if (gs.getImageFullUrl() != null && !gs.getImageFullUrl().isBlank()) {
            WpMediaLookup mediaLookup = resolveMedia(gs.getImageFullUrl());
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

        // Size attribute (resolve from lookup)
        List<ProductAttribute> attributes = new ArrayList<>();
        ProductAttribute sizeAttr = new ProductAttribute();
        Optional<WpAttributeLookup> attrLookup = wpUploadService.getAttributeByName(config.getSizeAttributeName());
        if (attrLookup.isPresent()) {
            sizeAttr.setId(attrLookup.get().getWordpressId());
        } else {
            logger.warn("Attribute '{}' not found in lookup table. Run wp-upload first.", config.getSizeAttributeName());
        }
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
        if (size.getOfferPrice() != null) {
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

        // Size attribute
        List<VariationAttribute> attributes = new ArrayList<>();
        VariationAttribute sizeAttr = new VariationAttribute();
        Optional<WpAttributeLookup> attrLookup = wpUploadService.getAttributeByName(config.getSizeAttributeName());
        if (attrLookup.isPresent()) {
            sizeAttr.setId(attrLookup.get().getWordpressId());
        }
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
            logger.error("Failed to upload media '{}': {}", imageUrl, e.getMessage());
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

    // ========== EXCEPTION ==========

    public static class AssortmentSyncException extends RuntimeException {
        public AssortmentSyncException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
