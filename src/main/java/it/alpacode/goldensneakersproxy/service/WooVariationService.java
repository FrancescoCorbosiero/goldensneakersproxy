package it.alpacode.goldensneakersproxy.service;

import it.alpacode.goldensneakersproxy.entity.woocommerce.*;
import it.alpacode.goldensneakersproxy.repository.WooVariationRepository;
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
public class WooVariationService {

    private static final Logger logger = LoggerFactory.getLogger(WooVariationService.class);

    private final WooVariationRepository variationRepository;

    public WooVariationService(WooVariationRepository variationRepository) {
        this.variationRepository = variationRepository;
    }

    // ========== CREATE ==========

    public WooVariation createVariation(WooVariation variation) {
        logger.info("Creating new variation for product ID: {}", variation.getProductId());

        OffsetDateTime now = OffsetDateTime.now();
        variation.setDateCreated(now);
        variation.setDateCreatedGmt(now);
        variation.setDateModified(now);
        variation.setDateModifiedGmt(now);

        setChildRelationships(variation);

        WooVariation saved = variationRepository.save(variation);
        logger.info("Variation created with ID: {}", saved.getId());
        return saved;
    }

    public WooVariation createVariation(Long productId, WooVariation variation) {
        variation.setProductId(productId);
        return createVariation(variation);
    }

    // ========== READ ==========

    @Transactional(readOnly = true)
    public Optional<WooVariation> getVariationById(Long id) {
        logger.debug("Fetching variation by ID: {}", id);
        return variationRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<WooVariation> getVariationBySku(String sku) {
        logger.debug("Fetching variation by SKU: {}", sku);
        return variationRepository.findBySku(sku);
    }

    @Transactional(readOnly = true)
    public List<WooVariation> getAllVariations() {
        logger.debug("Fetching all variations");
        return variationRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<WooVariation> getAllVariations(Pageable pageable) {
        logger.debug("Fetching variations with pagination: {}", pageable);
        return variationRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<WooVariation> getVariationsByProductId(Long productId) {
        logger.debug("Fetching variations for product ID: {}", productId);
        return variationRepository.findByProductId(productId);
    }

    @Transactional(readOnly = true)
    public Page<WooVariation> getVariationsByProductId(Long productId, Pageable pageable) {
        logger.debug("Fetching variations for product ID: {} with pagination", productId);
        return variationRepository.findByProductId(productId, pageable);
    }

    @Transactional(readOnly = true)
    public List<WooVariation> getVariationsByStatus(String status) {
        logger.debug("Fetching variations by status: {}", status);
        return variationRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<WooVariation> getVariationsByStockStatus(String stockStatus) {
        logger.debug("Fetching variations by stock status: {}", stockStatus);
        return variationRepository.findByStockStatus(stockStatus);
    }

    @Transactional(readOnly = true)
    public List<WooVariation> getVariationsByProductIdAndStatus(Long productId, String status) {
        logger.debug("Fetching variations for product ID: {} with status: {}", productId, status);
        return variationRepository.findByProductIdAndStatus(productId, status);
    }

    @Transactional(readOnly = true)
    public List<WooVariation> getVariationsByProductIdAndStockStatus(Long productId, String stockStatus) {
        logger.debug("Fetching variations for product ID: {} with stock status: {}", productId, stockStatus);
        return variationRepository.findByProductIdAndStockStatus(productId, stockStatus);
    }

    // ========== UPDATE ==========

    public WooVariation updateVariation(Long id, WooVariation variationDetails) {
        logger.info("Updating variation with ID: {}", id);

        return variationRepository.findById(id)
            .map(existingVariation -> {
                updateVariationFields(existingVariation, variationDetails);

                OffsetDateTime now = OffsetDateTime.now();
                existingVariation.setDateModified(now);
                existingVariation.setDateModifiedGmt(now);

                WooVariation updated = variationRepository.save(existingVariation);
                logger.info("Variation updated successfully: {}", updated.getId());
                return updated;
            })
            .orElseThrow(() -> new VariationNotFoundException(id));
    }

    public WooVariation patchVariation(Long id, WooVariation partialVariation) {
        logger.info("Patching variation with ID: {}", id);

        return variationRepository.findById(id)
            .map(existingVariation -> {
                patchVariationFields(existingVariation, partialVariation);

                OffsetDateTime now = OffsetDateTime.now();
                existingVariation.setDateModified(now);
                existingVariation.setDateModifiedGmt(now);

                WooVariation patched = variationRepository.save(existingVariation);
                logger.info("Variation patched successfully: {}", patched.getId());
                return patched;
            })
            .orElseThrow(() -> new VariationNotFoundException(id));
    }

    // ========== DELETE ==========

    public void deleteVariation(Long id) {
        logger.info("Deleting variation with ID: {}", id);

        if (!variationRepository.existsById(id)) {
            throw new VariationNotFoundException(id);
        }

        variationRepository.deleteById(id);
        logger.info("Variation deleted successfully: {}", id);
    }

    public void deleteVariationsByProductId(Long productId) {
        logger.info("Deleting all variations for product ID: {}", productId);
        variationRepository.deleteByProductId(productId);
        logger.info("All variations deleted for product ID: {}", productId);
    }

    public void deleteAllVariations() {
        logger.warn("Deleting all variations");
        variationRepository.deleteAll();
        logger.info("All variations deleted");
    }

    // ========== STATISTICS ==========

    @Transactional(readOnly = true)
    public long countVariations() {
        return variationRepository.count();
    }

    @Transactional(readOnly = true)
    public long countVariationsByProductId(Long productId) {
        return variationRepository.countByProductId(productId);
    }

    @Transactional(readOnly = true)
    public long countVariationsByStatus(String status) {
        return variationRepository.countByStatus(status);
    }

    // ========== HELPER METHODS ==========

    private void setChildRelationships(WooVariation variation) {
        if (variation.getDownloads() != null) {
            variation.getDownloads().forEach(d -> d.setVariation(variation));
        }
        if (variation.getAttributes() != null) {
            variation.getAttributes().forEach(a -> a.setVariation(variation));
        }
        if (variation.getMetaData() != null) {
            variation.getMetaData().forEach(m -> m.setVariation(variation));
        }
    }

    private void updateVariationFields(WooVariation existing, WooVariation details) {
        existing.setProductId(details.getProductId());
        existing.setDescription(details.getDescription());
        existing.setSku(details.getSku());
        existing.setGlobalUniqueId(details.getGlobalUniqueId());
        existing.setRegularPrice(details.getRegularPrice());
        existing.setSalePrice(details.getSalePrice());
        existing.setDateOnSaleFrom(details.getDateOnSaleFrom());
        existing.setDateOnSaleFromGmt(details.getDateOnSaleFromGmt());
        existing.setDateOnSaleTo(details.getDateOnSaleTo());
        existing.setDateOnSaleToGmt(details.getDateOnSaleToGmt());
        existing.setStatus(details.getStatus());
        existing.setVirtual(details.getVirtual());
        existing.setDownloadable(details.getDownloadable());
        existing.setDownloadLimit(details.getDownloadLimit());
        existing.setDownloadExpiry(details.getDownloadExpiry());
        existing.setTaxStatus(details.getTaxStatus());
        existing.setTaxClass(details.getTaxClass());
        existing.setManageStock(details.getManageStock());
        existing.setStockQuantity(details.getStockQuantity());
        existing.setStockStatus(details.getStockStatus());
        existing.setBackorders(details.getBackorders());
        existing.setLowStockAmount(details.getLowStockAmount());
        existing.setWeight(details.getWeight());
        existing.setDimensions(details.getDimensions());
        existing.setShippingClass(details.getShippingClass());
        existing.setImage(details.getImage());
        existing.setMenuOrder(details.getMenuOrder());

        updateCollections(existing, details);
    }

    private void updateCollections(WooVariation existing, WooVariation details) {
        existing.getDownloads().clear();
        if (details.getDownloads() != null) {
            details.getDownloads().forEach(existing::addDownload);
        }

        existing.getAttributes().clear();
        if (details.getAttributes() != null) {
            details.getAttributes().forEach(existing::addAttribute);
        }

        existing.getMetaData().clear();
        if (details.getMetaData() != null) {
            details.getMetaData().forEach(existing::addMetaData);
        }
    }

    private void patchVariationFields(WooVariation existing, WooVariation partial) {
        if (partial.getProductId() != null) existing.setProductId(partial.getProductId());
        if (partial.getDescription() != null) existing.setDescription(partial.getDescription());
        if (partial.getSku() != null) existing.setSku(partial.getSku());
        if (partial.getGlobalUniqueId() != null) existing.setGlobalUniqueId(partial.getGlobalUniqueId());
        if (partial.getRegularPrice() != null) existing.setRegularPrice(partial.getRegularPrice());
        if (partial.getSalePrice() != null) existing.setSalePrice(partial.getSalePrice());
        if (partial.getDateOnSaleFrom() != null) existing.setDateOnSaleFrom(partial.getDateOnSaleFrom());
        if (partial.getDateOnSaleFromGmt() != null) existing.setDateOnSaleFromGmt(partial.getDateOnSaleFromGmt());
        if (partial.getDateOnSaleTo() != null) existing.setDateOnSaleTo(partial.getDateOnSaleTo());
        if (partial.getDateOnSaleToGmt() != null) existing.setDateOnSaleToGmt(partial.getDateOnSaleToGmt());
        if (partial.getStatus() != null) existing.setStatus(partial.getStatus());
        if (partial.getVirtual() != null) existing.setVirtual(partial.getVirtual());
        if (partial.getDownloadable() != null) existing.setDownloadable(partial.getDownloadable());
        if (partial.getDownloadLimit() != null) existing.setDownloadLimit(partial.getDownloadLimit());
        if (partial.getDownloadExpiry() != null) existing.setDownloadExpiry(partial.getDownloadExpiry());
        if (partial.getTaxStatus() != null) existing.setTaxStatus(partial.getTaxStatus());
        if (partial.getTaxClass() != null) existing.setTaxClass(partial.getTaxClass());
        if (partial.getManageStock() != null) existing.setManageStock(partial.getManageStock());
        if (partial.getStockQuantity() != null) existing.setStockQuantity(partial.getStockQuantity());
        if (partial.getStockStatus() != null) existing.setStockStatus(partial.getStockStatus());
        if (partial.getBackorders() != null) existing.setBackorders(partial.getBackorders());
        if (partial.getLowStockAmount() != null) existing.setLowStockAmount(partial.getLowStockAmount());
        if (partial.getWeight() != null) existing.setWeight(partial.getWeight());
        if (partial.getDimensions() != null) existing.setDimensions(partial.getDimensions());
        if (partial.getShippingClass() != null) existing.setShippingClass(partial.getShippingClass());
        if (partial.getImage() != null) existing.setImage(partial.getImage());
        if (partial.getMenuOrder() != null) existing.setMenuOrder(partial.getMenuOrder());

        if (partial.getDownloads() != null && !partial.getDownloads().isEmpty()) {
            existing.getDownloads().clear();
            partial.getDownloads().forEach(existing::addDownload);
        }
        if (partial.getAttributes() != null && !partial.getAttributes().isEmpty()) {
            existing.getAttributes().clear();
            partial.getAttributes().forEach(existing::addAttribute);
        }
        if (partial.getMetaData() != null && !partial.getMetaData().isEmpty()) {
            existing.getMetaData().clear();
            partial.getMetaData().forEach(existing::addMetaData);
        }
    }

    // ========== EXCEPTION CLASS ==========

    public static class VariationNotFoundException extends RuntimeException {
        public VariationNotFoundException(Long id) {
            super("Variation not found with ID: " + id);
        }
    }
}
