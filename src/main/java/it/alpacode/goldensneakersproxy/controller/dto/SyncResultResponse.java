package it.alpacode.goldensneakersproxy.controller.dto;

import it.alpacode.goldensneakersproxy.model.SyncResult;

import java.util.List;

/**
 * Response DTO for sync result (simplified).
 */
public class SyncResultResponse {

    private int createdCount;
    private int updatedCount;
    private int markedOutOfStockCount;
    private int taxonomiesCreated;
    private int totalVariationsCount;
    private long durationMs;
    private String status;
    private String errorMessage;
    private List<String> warnings;

    public SyncResultResponse() {
    }

    public static SyncResultResponse from(SyncResult result) {
        SyncResultResponse response = new SyncResultResponse();
        response.setCreatedCount(result.getCreatedCount());
        response.setUpdatedCount(result.getUpdatedCount());
        response.setMarkedOutOfStockCount(result.getMarkedOutOfStockCount());
        response.setTaxonomiesCreated(result.getTaxonomiesCreated());
        response.setTotalVariationsCount(result.getTotalVariationsCount());
        response.setDurationMs(result.getDurationMs());
        response.setStatus(result.getStatus());
        response.setErrorMessage(result.getErrorMessage());
        response.setWarnings(result.getWarnings());
        return response;
    }

    // Getters and setters

    public int getCreatedCount() {
        return createdCount;
    }

    public void setCreatedCount(int createdCount) {
        this.createdCount = createdCount;
    }

    public int getUpdatedCount() {
        return updatedCount;
    }

    public void setUpdatedCount(int updatedCount) {
        this.updatedCount = updatedCount;
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

    public int getTotalVariationsCount() {
        return totalVariationsCount;
    }

    public void setTotalVariationsCount(int totalVariationsCount) {
        this.totalVariationsCount = totalVariationsCount;
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
}
