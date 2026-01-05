package it.alpacode.goldensneakersproxy.model;

import it.alpacode.goldensneakersproxy.client.woocommerce.dto.ProductDto;
import it.alpacode.goldensneakersproxy.client.woocommerce.dto.VariationDto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Result of a catalog sync operation.
 */
public class SyncResult {

    private List<ProductDto> created = new ArrayList<>();
    private List<ProductDto> updated = new ArrayList<>();
    private int markedOutOfStockCount;
    private int taxonomiesCreated;
    private Map<Long, List<VariationDto>> variations = new HashMap<>();
    private long durationMs;
    private String status; // SUCCESS, FAILED, PARTIAL
    private String errorMessage;
    private List<String> warnings = new ArrayList<>();

    public SyncResult() {
    }

    public List<ProductDto> getCreated() {
        return created;
    }

    public void setCreated(List<ProductDto> created) {
        this.created = created;
    }

    public List<ProductDto> getUpdated() {
        return updated;
    }

    public void setUpdated(List<ProductDto> updated) {
        this.updated = updated;
    }

    public int getMarkedOutOfStockCount() {
        return markedOutOfStockCount;
    }

    public void setMarkedOutOfStockCount(int markedOutOfStockCount) {
        this.markedOutOfStockCount = markedOutOfStockCount;
    }

    public int getTaxonomiesCreated() {
        return taxonomiesCreated;
    }

    public void setTaxonomiesCreated(int taxonomiesCreated) {
        this.taxonomiesCreated = taxonomiesCreated;
    }

    public Map<Long, List<VariationDto>> getVariations() {
        return variations;
    }

    public void setVariations(Map<Long, List<VariationDto>> variations) {
        this.variations = variations;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    // Convenience methods

    public int getCreatedCount() {
        return created.size();
    }

    public int getUpdatedCount() {
        return updated.size();
    }

    public int getTotalVariationsCount() {
        return variations.values().stream().mapToInt(List::size).sum();
    }

    public void addCreated(List<ProductDto> products) {
        created.addAll(products);
    }

    public void addUpdated(List<ProductDto> products) {
        updated.addAll(products);
    }

    public void addMarkedOutOfStock(int count) {
        markedOutOfStockCount += count;
    }

    public void addWarning(String warning) {
        warnings.add(warning);
    }

    /**
     * Create a success result.
     */
    public static SyncResult success() {
        SyncResult result = new SyncResult();
        result.setStatus("SUCCESS");
        return result;
    }

    /**
     * Create a failed result.
     */
    public static SyncResult failed(String errorMessage) {
        SyncResult result = new SyncResult();
        result.setStatus("FAILED");
        result.setErrorMessage(errorMessage);
        return result;
    }
}
