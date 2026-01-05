package it.alpacode.goldensneakersproxy.service;

import it.alpacode.goldensneakersproxy.client.woocommerce.WooCommerceClient;
import it.alpacode.goldensneakersproxy.client.woocommerce.dto.ProductDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Transaction manager for catalog sync operations.
 * Tracks created products and provides rollback capability.
 */
@Service
public class SyncTransactionManager {

    private static final Logger log = LoggerFactory.getLogger(SyncTransactionManager.class);

    private final WooCommerceClient wooClient;
    private final Map<String, List<Long>> transactionCreatedProducts = new ConcurrentHashMap<>();

    public SyncTransactionManager(WooCommerceClient wooClient) {
        this.wooClient = wooClient;
    }

    /**
     * Begin a new transaction.
     *
     * @return Transaction ID
     */
    public String begin() {
        String txId = UUID.randomUUID().toString();
        transactionCreatedProducts.put(txId, new ArrayList<>());
        log.info("Transaction {} started", txId);
        return txId;
    }

    /**
     * Track products created during a transaction.
     */
    public void trackCreated(String txId, List<ProductDto> products) {
        List<Long> ids = products.stream()
                .map(ProductDto::getId)
                .toList();
        List<Long> tracked = transactionCreatedProducts.get(txId);
        if (tracked != null) {
            tracked.addAll(ids);
            log.debug("Transaction {} - tracked {} created products", txId, ids.size());
        }
    }

    /**
     * Track a single product created during a transaction.
     */
    public void trackCreated(String txId, Long productId) {
        List<Long> tracked = transactionCreatedProducts.get(txId);
        if (tracked != null) {
            tracked.add(productId);
            log.debug("Transaction {} - tracked product {}", txId, productId);
        }
    }

    /**
     * Commit a transaction - removes tracking data.
     */
    public void commit(String txId) {
        List<Long> createdIds = transactionCreatedProducts.remove(txId);
        if (createdIds != null) {
            log.info("Transaction {} committed ({} products)", txId, createdIds.size());
        }
    }

    /**
     * Rollback a transaction - deletes all created products.
     */
    public void rollback(String txId) {
        List<Long> createdIds = transactionCreatedProducts.get(txId);

        if (createdIds != null && !createdIds.isEmpty()) {
            log.warn("Rolling back transaction {}, deleting {} products",
                    txId, createdIds.size());

            try {
                wooClient.deleteProductsBatch(createdIds);
                log.info("Rollback completed for transaction {} - deleted {} products",
                        txId, createdIds.size());
            } catch (Exception e) {
                log.error("Rollback failed for transaction {} - some products may remain: {}",
                        txId, e.getMessage());
            }
        } else {
            log.info("Transaction {} rollback - no products to delete", txId);
        }

        transactionCreatedProducts.remove(txId);
    }

    /**
     * Check if a transaction is active.
     */
    public boolean isActive(String txId) {
        return transactionCreatedProducts.containsKey(txId);
    }

    /**
     * Get the count of products tracked in a transaction.
     */
    public int getTrackedCount(String txId) {
        List<Long> tracked = transactionCreatedProducts.get(txId);
        return tracked != null ? tracked.size() : 0;
    }
}
