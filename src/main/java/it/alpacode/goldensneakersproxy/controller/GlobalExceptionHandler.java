package it.alpacode.goldensneakersproxy.controller;

import it.alpacode.goldensneakersproxy.service.AssortmentMapperService;
import it.alpacode.goldensneakersproxy.service.GoldenSneakersClient;
import it.alpacode.goldensneakersproxy.service.WordPressClient;
import it.alpacode.goldensneakersproxy.service.WordPressUploadService;
import it.alpacode.goldensneakersproxy.service.WooProductService;
import it.alpacode.goldensneakersproxy.service.WooVariationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler for all controllers.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(GoldenSneakersClient.GoldenSneakersApiException.class)
    public ResponseEntity<ErrorResponse> handleGoldenSneakersApiException(
            GoldenSneakersClient.GoldenSneakersApiException ex) {
        logger.error("GoldenSneakers API error: {}", ex.getMessage());

        int statusCode = ex.getStatusCode();
        String message = ex.getMessage();

        // If it's an authentication error from GoldenSneakers, return 502 Bad Gateway
        if (statusCode == 401 || statusCode == 403) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorResponse("upstream_auth_error",
                    "Authentication failed with GoldenSneakers API. JWT token may be expired or invalid."));
        }

        // For 4xx errors, try to pass through the upstream error message
        if (statusCode >= 400 && statusCode < 500) {
            return ResponseEntity.status(statusCode)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorResponse("upstream_error", message));
        }

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
            .contentType(MediaType.APPLICATION_JSON)
            .body(new ErrorResponse("upstream_error", message));
    }

    @ExceptionHandler(WooProductService.ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProductNotFoundException(
            WooProductService.ProductNotFoundException ex) {
        logger.warn("Product not found: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .contentType(MediaType.APPLICATION_JSON)
            .body(new ErrorResponse("product_not_found", ex.getMessage()));
    }

    @ExceptionHandler(WooVariationService.VariationNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleVariationNotFoundException(
            WooVariationService.VariationNotFoundException ex) {
        logger.warn("Variation not found: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .contentType(MediaType.APPLICATION_JSON)
            .body(new ErrorResponse("variation_not_found", ex.getMessage()));
    }

    @ExceptionHandler(WordPressClient.WordPressApiException.class)
    public ResponseEntity<ErrorResponse> handleWordPressApiException(
            WordPressClient.WordPressApiException ex) {
        logger.error("WordPress API error: {}", ex.getMessage());

        int statusCode = ex.getStatusCode();
        String message = ex.getMessage();

        if (statusCode == 401 || statusCode == 403) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorResponse("wordpress_auth_error",
                    "Authentication failed with WordPress API. Consumer key/secret may be invalid."));
        }

        if (statusCode >= 400 && statusCode < 500) {
            return ResponseEntity.status(statusCode)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorResponse("wordpress_error", message));
        }

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
            .contentType(MediaType.APPLICATION_JSON)
            .body(new ErrorResponse("wordpress_error", message));
    }

    @ExceptionHandler(AssortmentMapperService.AssortmentSyncException.class)
    public ResponseEntity<ErrorResponse> handleAssortmentSyncException(
            AssortmentMapperService.AssortmentSyncException ex) {
        logger.error("Assortment sync error: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .contentType(MediaType.APPLICATION_JSON)
            .body(new ErrorResponse("assortment_sync_error", ex.getMessage()));
    }

    @ExceptionHandler(WordPressUploadService.WordPressUploadException.class)
    public ResponseEntity<ErrorResponse> handleWordPressUploadException(
            WordPressUploadService.WordPressUploadException ex) {
        logger.error("WordPress upload error: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
            .contentType(MediaType.APPLICATION_JSON)
            .body(new ErrorResponse("wordpress_upload_error", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        logger.error("Unexpected error: {}", ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .contentType(MediaType.APPLICATION_JSON)
            .body(new ErrorResponse("internal_error", "An unexpected error occurred: " + ex.getMessage()));
    }

    public record ErrorResponse(String error, String message) {}
}
