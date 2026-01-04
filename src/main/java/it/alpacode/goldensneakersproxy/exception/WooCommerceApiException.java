package it.alpacode.goldensneakersproxy.exception;

/**
 * Exception thrown when WooCommerce API calls fail.
 */
public class WooCommerceApiException extends RuntimeException {

    private final int statusCode;
    private final String responseBody;

    public WooCommerceApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
        this.responseBody = null;
    }

    public WooCommerceApiException(String message, int statusCode, String responseBody) {
        super(message);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public WooCommerceApiException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 0;
        this.responseBody = null;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
