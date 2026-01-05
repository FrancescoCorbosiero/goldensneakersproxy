package it.alpacode.goldensneakersproxy.exception;

/**
 * Exception thrown when catalog synchronization fails.
 */
public class CatalogSyncException extends RuntimeException {

    private final boolean rolledBack;

    public CatalogSyncException(String message) {
        super(message);
        this.rolledBack = false;
    }

    public CatalogSyncException(String message, Throwable cause) {
        super(message, cause);
        this.rolledBack = false;
    }

    public CatalogSyncException(String message, Throwable cause, boolean rolledBack) {
        super(message, cause);
        this.rolledBack = rolledBack;
    }

    public boolean isRolledBack() {
        return rolledBack;
    }
}
