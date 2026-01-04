package it.alpacode.goldensneakersproxy.service;

import it.alpacode.goldensneakersproxy.client.woocommerce.dto.ProductDto;
import it.alpacode.goldensneakersproxy.model.CatalogDiff;
import it.alpacode.goldensneakersproxy.model.CatalogProduct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for calculating differences between feed products and shop products.
 */
@Service
public class ProductDiffService {

    private static final Logger log = LoggerFactory.getLogger(ProductDiffService.class);

    /**
     * Calculate the diff between feed products and existing shop products.
     *
     * @param feedProducts List of products from the feed
     * @param shopProducts Map of existing products in shop (SKU -> ProductDto)
     * @return CatalogDiff containing products to create, update, and mark out of stock
     */
    public CatalogDiff calculateDiff(
            List<CatalogProduct> feedProducts,
            Map<String, ProductDto> shopProducts) {

        Set<String> feedSkus = feedProducts.stream()
                .map(CatalogProduct::getSku)
                .collect(Collectors.toSet());

        Set<String> shopSkus = shopProducts.keySet();

        // Products to CREATE (in feed, not in shop)
        List<CatalogProduct> toCreate = feedProducts.stream()
                .filter(p -> !shopSkus.contains(p.getSku()))
                .toList();

        // Products to UPDATE (in both feed and shop)
        List<CatalogProduct> toUpdate = feedProducts.stream()
                .filter(p -> shopSkus.contains(p.getSku()))
                .toList();

        // Products to MARK OUT OF STOCK (in shop, not in feed, currently in stock)
        List<Long> toMarkOutOfStock = shopProducts.entrySet().stream()
                .filter(e -> !feedSkus.contains(e.getKey()))
                .filter(e -> !"outofstock".equals(e.getValue().getStockStatus()))
                .map(e -> e.getValue().getId())
                .toList();

        CatalogDiff diff = new CatalogDiff(toCreate, toUpdate, toMarkOutOfStock);

        log.info("Diff calculation complete - Create: {}, Update: {}, MarkOutOfStock: {}",
                toCreate.size(), toUpdate.size(), toMarkOutOfStock.size());

        return diff;
    }

    /**
     * Check if a product needs updating by comparing relevant fields.
     * This can be used for more intelligent diffing if needed.
     */
    public boolean needsUpdate(CatalogProduct feedProduct, ProductDto shopProduct) {
        // For now, we always update to ensure data is fresh
        // Could be enhanced to compare specific fields
        return true;
    }
}
