package it.alpacode.goldensneakersproxy.service;

import it.alpacode.goldensneakersproxy.entity.woocommerce.*;
import it.alpacode.goldensneakersproxy.repository.WooProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class WooProductService {

    private static final Logger logger = LoggerFactory.getLogger(WooProductService.class);

    private final WooProductRepository productRepository;

    public WooProductService(WooProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // ========== CREATE ==========

    public WooProduct createProduct(WooProduct product) {
        logger.info("Creating new product: {}", product.getName());

        OffsetDateTime now = OffsetDateTime.now();
        product.setDateCreated(now);
        product.setDateCreatedGmt(now);
        product.setDateModified(now);
        product.setDateModifiedGmt(now);

        setChildRelationships(product);

        WooProduct saved = productRepository.save(product);
        logger.info("Product created with ID: {}", saved.getId());
        return saved;
    }

    // ========== READ ==========

    @Transactional(readOnly = true)
    public Optional<WooProduct> getProductById(Long id) {
        logger.debug("Fetching product by ID: {}", id);
        return productRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<WooProduct> getProductBySku(String sku) {
        logger.debug("Fetching product by SKU: {}", sku);
        return productRepository.findBySku(sku);
    }

    @Transactional(readOnly = true)
    public Optional<WooProduct> getProductBySlug(String slug) {
        logger.debug("Fetching product by slug: {}", slug);
        return productRepository.findBySlug(slug);
    }

    @Transactional(readOnly = true)
    public List<WooProduct> getAllProducts() {
        logger.debug("Fetching all products");
        return productRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<WooProduct> getAllProducts(Pageable pageable) {
        logger.debug("Fetching products with pagination: {}", pageable);
        return productRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<WooProduct> getProductsByType(String type) {
        logger.debug("Fetching products by type: {}", type);
        return productRepository.findByType(type);
    }

    @Transactional(readOnly = true)
    public List<WooProduct> getProductsByStatus(String status) {
        logger.debug("Fetching products by status: {}", status);
        return productRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public Page<WooProduct> getProductsByStatus(String status, Pageable pageable) {
        logger.debug("Fetching products by status: {} with pagination", status);
        return productRepository.findByStatus(status, pageable);
    }

    @Transactional(readOnly = true)
    public List<WooProduct> getFeaturedProducts() {
        logger.debug("Fetching featured products");
        return productRepository.findByFeatured(true);
    }

    @Transactional(readOnly = true)
    public List<WooProduct> searchProducts(String keyword) {
        logger.debug("Searching products with keyword: {}", keyword);
        return productRepository.searchByKeyword(keyword);
    }

    @Transactional(readOnly = true)
    public Page<WooProduct> searchProducts(String keyword, Pageable pageable) {
        logger.debug("Searching products with keyword: {} and pagination", keyword);
        return productRepository.searchByKeyword(keyword, pageable);
    }

    @Transactional(readOnly = true)
    public List<WooProduct> getProductsByStockStatus(String stockStatus) {
        logger.debug("Fetching products by stock status: {}", stockStatus);
        return productRepository.findByStockStatus(stockStatus);
    }

    @Transactional(readOnly = true)
    public List<WooProduct> getProductsByCategoryId(Integer categoryId) {
        logger.debug("Fetching products by category ID: {}", categoryId);
        return productRepository.findByCategoryId(categoryId);
    }

    @Transactional(readOnly = true)
    public List<WooProduct> getProductsByTagId(Integer tagId) {
        logger.debug("Fetching products by tag ID: {}", tagId);
        return productRepository.findByTagId(tagId);
    }

    // ========== UPDATE ==========

    public WooProduct updateProduct(Long id, WooProduct productDetails) {
        logger.info("Updating product with ID: {}", id);

        return productRepository.findById(id)
            .map(existingProduct -> {
                updateProductFields(existingProduct, productDetails);

                OffsetDateTime now = OffsetDateTime.now();
                existingProduct.setDateModified(now);
                existingProduct.setDateModifiedGmt(now);

                WooProduct updated = productRepository.save(existingProduct);
                logger.info("Product updated successfully: {}", updated.getId());
                return updated;
            })
            .orElseThrow(() -> new ProductNotFoundException(id));
    }

    public WooProduct patchProduct(Long id, WooProduct partialProduct) {
        logger.info("Patching product with ID: {}", id);

        return productRepository.findById(id)
            .map(existingProduct -> {
                patchProductFields(existingProduct, partialProduct);

                OffsetDateTime now = OffsetDateTime.now();
                existingProduct.setDateModified(now);
                existingProduct.setDateModifiedGmt(now);

                WooProduct patched = productRepository.save(existingProduct);
                logger.info("Product patched successfully: {}", patched.getId());
                return patched;
            })
            .orElseThrow(() -> new ProductNotFoundException(id));
    }

    // ========== DELETE ==========

    public void deleteProduct(Long id) {
        logger.info("Deleting product with ID: {}", id);

        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }

        productRepository.deleteById(id);
        logger.info("Product deleted successfully: {}", id);
    }

    public void deleteAllProducts() {
        logger.warn("Deleting all products");
        productRepository.deleteAll();
        logger.info("All products deleted");
    }

    // ========== STATISTICS ==========

    @Transactional(readOnly = true)
    public long countProducts() {
        return productRepository.count();
    }

    @Transactional(readOnly = true)
    public long countProductsByStatus(String status) {
        return productRepository.countByStatus(status);
    }

    @Transactional(readOnly = true)
    public long countProductsByType(String type) {
        return productRepository.countByType(type);
    }

    @Transactional(readOnly = true)
    public List<String> getAllProductTypes() {
        return productRepository.findAllProductTypes();
    }

    @Transactional(readOnly = true)
    public List<String> getAllStatuses() {
        return productRepository.findAllStatuses();
    }

    // ========== HELPER METHODS ==========

    private void setChildRelationships(WooProduct product) {
        if (product.getDownloads() != null) {
            product.getDownloads().forEach(d -> d.setProduct(product));
        }
        if (product.getCategories() != null) {
            product.getCategories().forEach(c -> c.setProduct(product));
        }
        if (product.getTags() != null) {
            product.getTags().forEach(t -> t.setProduct(product));
        }
        if (product.getImages() != null) {
            product.getImages().forEach(i -> i.setProduct(product));
        }
        if (product.getAttributes() != null) {
            product.getAttributes().forEach(a -> a.setProduct(product));
        }
        if (product.getDefaultAttributes() != null) {
            product.getDefaultAttributes().forEach(da -> da.setProduct(product));
        }
        if (product.getMetaData() != null) {
            product.getMetaData().forEach(m -> m.setProduct(product));
        }
    }

    private void updateProductFields(WooProduct existing, WooProduct details) {
        existing.setName(details.getName());
        existing.setSlug(details.getSlug());
        existing.setType(details.getType());
        existing.setStatus(details.getStatus());
        existing.setFeatured(details.getFeatured());
        existing.setCatalogVisibility(details.getCatalogVisibility());
        existing.setDescription(details.getDescription());
        existing.setShortDescription(details.getShortDescription());
        existing.setSku(details.getSku());
        existing.setGlobalUniqueId(details.getGlobalUniqueId());
        existing.setRegularPrice(details.getRegularPrice());
        existing.setSalePrice(details.getSalePrice());
        existing.setDateOnSaleFrom(details.getDateOnSaleFrom());
        existing.setDateOnSaleFromGmt(details.getDateOnSaleFromGmt());
        existing.setDateOnSaleTo(details.getDateOnSaleTo());
        existing.setDateOnSaleToGmt(details.getDateOnSaleToGmt());
        existing.setVirtual(details.getVirtual());
        existing.setDownloadable(details.getDownloadable());
        existing.setDownloadLimit(details.getDownloadLimit());
        existing.setDownloadExpiry(details.getDownloadExpiry());
        existing.setExternalUrl(details.getExternalUrl());
        existing.setButtonText(details.getButtonText());
        existing.setTaxStatus(details.getTaxStatus());
        existing.setTaxClass(details.getTaxClass());
        existing.setManageStock(details.getManageStock());
        existing.setStockQuantity(details.getStockQuantity());
        existing.setStockStatus(details.getStockStatus());
        existing.setBackorders(details.getBackorders());
        existing.setLowStockAmount(details.getLowStockAmount());
        existing.setSoldIndividually(details.getSoldIndividually());
        existing.setWeight(details.getWeight());
        existing.setDimensions(details.getDimensions());
        existing.setShippingClass(details.getShippingClass());
        existing.setReviewsAllowed(details.getReviewsAllowed());
        existing.setUpsellIds(details.getUpsellIds());
        existing.setCrossSellIds(details.getCrossSellIds());
        existing.setParentId(details.getParentId());
        existing.setPurchaseNote(details.getPurchaseNote());
        existing.setGroupedProducts(details.getGroupedProducts());
        existing.setMenuOrder(details.getMenuOrder());

        updateCollections(existing, details);
    }

    private void updateCollections(WooProduct existing, WooProduct details) {
        existing.getDownloads().clear();
        if (details.getDownloads() != null) {
            details.getDownloads().forEach(existing::addDownload);
        }

        existing.getCategories().clear();
        if (details.getCategories() != null) {
            details.getCategories().forEach(existing::addCategory);
        }

        existing.getTags().clear();
        if (details.getTags() != null) {
            details.getTags().forEach(existing::addTag);
        }

        existing.getImages().clear();
        if (details.getImages() != null) {
            details.getImages().forEach(existing::addImage);
        }

        existing.getAttributes().clear();
        if (details.getAttributes() != null) {
            details.getAttributes().forEach(existing::addAttribute);
        }

        existing.getDefaultAttributes().clear();
        if (details.getDefaultAttributes() != null) {
            details.getDefaultAttributes().forEach(existing::addDefaultAttribute);
        }

        existing.getMetaData().clear();
        if (details.getMetaData() != null) {
            details.getMetaData().forEach(existing::addMetaData);
        }
    }

    private void patchProductFields(WooProduct existing, WooProduct partial) {
        if (partial.getName() != null) existing.setName(partial.getName());
        if (partial.getSlug() != null) existing.setSlug(partial.getSlug());
        if (partial.getType() != null) existing.setType(partial.getType());
        if (partial.getStatus() != null) existing.setStatus(partial.getStatus());
        if (partial.getFeatured() != null) existing.setFeatured(partial.getFeatured());
        if (partial.getCatalogVisibility() != null) existing.setCatalogVisibility(partial.getCatalogVisibility());
        if (partial.getDescription() != null) existing.setDescription(partial.getDescription());
        if (partial.getShortDescription() != null) existing.setShortDescription(partial.getShortDescription());
        if (partial.getSku() != null) existing.setSku(partial.getSku());
        if (partial.getGlobalUniqueId() != null) existing.setGlobalUniqueId(partial.getGlobalUniqueId());
        if (partial.getRegularPrice() != null) existing.setRegularPrice(partial.getRegularPrice());
        if (partial.getSalePrice() != null) existing.setSalePrice(partial.getSalePrice());
        if (partial.getDateOnSaleFrom() != null) existing.setDateOnSaleFrom(partial.getDateOnSaleFrom());
        if (partial.getDateOnSaleFromGmt() != null) existing.setDateOnSaleFromGmt(partial.getDateOnSaleFromGmt());
        if (partial.getDateOnSaleTo() != null) existing.setDateOnSaleTo(partial.getDateOnSaleTo());
        if (partial.getDateOnSaleToGmt() != null) existing.setDateOnSaleToGmt(partial.getDateOnSaleToGmt());
        if (partial.getVirtual() != null) existing.setVirtual(partial.getVirtual());
        if (partial.getDownloadable() != null) existing.setDownloadable(partial.getDownloadable());
        if (partial.getDownloadLimit() != null) existing.setDownloadLimit(partial.getDownloadLimit());
        if (partial.getDownloadExpiry() != null) existing.setDownloadExpiry(partial.getDownloadExpiry());
        if (partial.getExternalUrl() != null) existing.setExternalUrl(partial.getExternalUrl());
        if (partial.getButtonText() != null) existing.setButtonText(partial.getButtonText());
        if (partial.getTaxStatus() != null) existing.setTaxStatus(partial.getTaxStatus());
        if (partial.getTaxClass() != null) existing.setTaxClass(partial.getTaxClass());
        if (partial.getManageStock() != null) existing.setManageStock(partial.getManageStock());
        if (partial.getStockQuantity() != null) existing.setStockQuantity(partial.getStockQuantity());
        if (partial.getStockStatus() != null) existing.setStockStatus(partial.getStockStatus());
        if (partial.getBackorders() != null) existing.setBackorders(partial.getBackorders());
        if (partial.getLowStockAmount() != null) existing.setLowStockAmount(partial.getLowStockAmount());
        if (partial.getSoldIndividually() != null) existing.setSoldIndividually(partial.getSoldIndividually());
        if (partial.getWeight() != null) existing.setWeight(partial.getWeight());
        if (partial.getDimensions() != null) existing.setDimensions(partial.getDimensions());
        if (partial.getShippingClass() != null) existing.setShippingClass(partial.getShippingClass());
        if (partial.getReviewsAllowed() != null) existing.setReviewsAllowed(partial.getReviewsAllowed());
        if (partial.getUpsellIds() != null) existing.setUpsellIds(partial.getUpsellIds());
        if (partial.getCrossSellIds() != null) existing.setCrossSellIds(partial.getCrossSellIds());
        if (partial.getParentId() != null) existing.setParentId(partial.getParentId());
        if (partial.getPurchaseNote() != null) existing.setPurchaseNote(partial.getPurchaseNote());
        if (partial.getGroupedProducts() != null) existing.setGroupedProducts(partial.getGroupedProducts());
        if (partial.getMenuOrder() != null) existing.setMenuOrder(partial.getMenuOrder());

        if (partial.getDownloads() != null && !partial.getDownloads().isEmpty()) {
            existing.getDownloads().clear();
            partial.getDownloads().forEach(existing::addDownload);
        }
        if (partial.getCategories() != null && !partial.getCategories().isEmpty()) {
            existing.getCategories().clear();
            partial.getCategories().forEach(existing::addCategory);
        }
        if (partial.getTags() != null && !partial.getTags().isEmpty()) {
            existing.getTags().clear();
            partial.getTags().forEach(existing::addTag);
        }
        if (partial.getImages() != null && !partial.getImages().isEmpty()) {
            existing.getImages().clear();
            partial.getImages().forEach(existing::addImage);
        }
        if (partial.getAttributes() != null && !partial.getAttributes().isEmpty()) {
            existing.getAttributes().clear();
            partial.getAttributes().forEach(existing::addAttribute);
        }
        if (partial.getDefaultAttributes() != null && !partial.getDefaultAttributes().isEmpty()) {
            existing.getDefaultAttributes().clear();
            partial.getDefaultAttributes().forEach(existing::addDefaultAttribute);
        }
        if (partial.getMetaData() != null && !partial.getMetaData().isEmpty()) {
            existing.getMetaData().clear();
            partial.getMetaData().forEach(existing::addMetaData);
        }
    }

    // ========== EXCEPTION CLASS ==========

    public static class ProductNotFoundException extends RuntimeException {
        public ProductNotFoundException(Long id) {
            super("Product not found with ID: " + id);
        }
    }
}
