package it.alpacode.goldensneakersproxy.controller;

import it.alpacode.goldensneakersproxy.service.GoldenSneakersClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
public class ProxyController {

    private static final Logger logger = LoggerFactory.getLogger(ProxyController.class);

    private final GoldenSneakersClient goldenSneakersClient;

    public ProxyController(GoldenSneakersClient goldenSneakersClient) {
        this.goldenSneakersClient = goldenSneakersClient;
    }

    // ========== Assortment Endpoints ==========

    /**
     * GET /api/assortment - List all products with available sizes
     */
    @GetMapping(value = "/api/assortment", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getAssortment(@RequestParam Map<String, String> queryParams) {
        logger.info("Received request to /api/assortment with params: {}", queryParams);

        String response = goldenSneakersClient.fetchAssortment(queryParams).block();

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(response);
    }

    /**
     * GET /api/assortment/{id} - Get a specific assortment by ID
     */
    @GetMapping(value = "/api/assortment/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getAssortmentById(
            @PathVariable String id,
            @RequestParam Map<String, String> queryParams) {
        logger.info("Received request to /api/assortment/{} with params: {}", id, queryParams);

        String response = goldenSneakersClient.fetchAssortmentById(id, queryParams).block();

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(response);
    }

    /**
     * Download endpoint for assortment list
     */
    @GetMapping(value = "/download/assortment.json", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> downloadAssortment(@RequestParam Map<String, String> queryParams) {
        logger.info("Received request to /download/assortment.json with params: {}", queryParams);

        String response = goldenSneakersClient.fetchAssortment(queryParams).block();
        byte[] content = response != null ? response.getBytes() : new byte[0];

        return createDownloadResponse(content, "assortment.json");
    }

    // ========== Assortment Flat Endpoints ==========

    /**
     * GET /api/assortment-flat - List all sizes as individual products (flat list)
     */
    @GetMapping(value = "/api/assortment-flat", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getAssortmentFlat(@RequestParam Map<String, String> queryParams) {
        logger.info("Received request to /api/assortment-flat with params: {}", queryParams);

        String response = goldenSneakersClient.fetchAssortmentFlat(queryParams).block();

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(response);
    }

    /**
     * GET /api/assortment-flat/{id} - Get a specific flat size by ID
     */
    @GetMapping(value = "/api/assortment-flat/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getAssortmentFlatById(
            @PathVariable String id,
            @RequestParam Map<String, String> queryParams) {
        logger.info("Received request to /api/assortment-flat/{} with params: {}", id, queryParams);

        String response = goldenSneakersClient.fetchAssortmentFlatById(id, queryParams).block();

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(response);
    }

    /**
     * Download endpoint for assortment-flat list
     */
    @GetMapping(value = "/download/assortment-flat.json", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> downloadAssortmentFlat(@RequestParam Map<String, String> queryParams) {
        logger.info("Received request to /download/assortment-flat.json with params: {}", queryParams);

        String response = goldenSneakersClient.fetchAssortmentFlat(queryParams).block();
        byte[] content = response != null ? response.getBytes() : new byte[0];

        return createDownloadResponse(content, "assortment-flat.json");
    }

    // ========== Assortment Size Endpoint ==========

    /**
     * GET /api/assortment-size - Retrieve product info by size ID or barcode
     */
    @GetMapping(value = "/api/assortment-size", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getAssortmentSize(@RequestParam Map<String, String> queryParams) {
        logger.info("Received request to /api/assortment-size with params: {}", queryParams);

        String response = goldenSneakersClient.fetchAssortmentSize(queryParams).block();

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(response);
    }

    // ========== SKU Search Endpoint ==========

    /**
     * GET /api/sku-search - Search products by SKU or name
     */
    @GetMapping(value = "/api/sku-search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> searchSku(@RequestParam Map<String, String> queryParams) {
        logger.info("Received request to /api/sku-search with params: {}", queryParams);

        String response = goldenSneakersClient.searchSku(queryParams).block();

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(response);
    }

    // ========== Orders Dropship Endpoints ==========

    /**
     * POST /api/orders-dropship/create-order - Create a dropship order
     */
    @PostMapping(value = "/api/orders-dropship/create-order",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> createDropshipOrder(@RequestBody String requestBody) {
        logger.info("Received request to /api/orders-dropship/create-order");

        String response = goldenSneakersClient.createDropshipOrder(requestBody).block();

        return ResponseEntity.status(HttpStatus.CREATED)
            .contentType(MediaType.APPLICATION_JSON)
            .body(response);
    }

    /**
     * GET /api/orders-dropship/order-details/{orderId} - Get dropship order details
     */
    @GetMapping(value = "/api/orders-dropship/order-details/{orderId}",
                produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getOrderDetails(@PathVariable String orderId) {
        logger.info("Received request to /api/orders-dropship/order-details/{}", orderId);

        String response = goldenSneakersClient.getOrderDetails(orderId).block();

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(response);
    }

    /**
     * GET /api/orders-dropship/package-details/{packageId} - Get dropship package details
     */
    @GetMapping(value = "/api/orders-dropship/package-details/{packageId}",
                produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getPackageDetails(@PathVariable String packageId) {
        logger.info("Received request to /api/orders-dropship/package-details/{}", packageId);

        String response = goldenSneakersClient.getPackageDetails(packageId).block();

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(response);
    }

    /**
     * POST /api/orders-dropship/upload-shipping-label/{orderId} - Upload shipping label
     */
    @PostMapping(value = "/api/orders-dropship/upload-shipping-label/{orderId}",
                 consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> uploadShippingLabel(
            @PathVariable String orderId,
            @RequestParam("shipping_label") MultipartFile shippingLabel,
            @RequestParam("tracking_numbers") String trackingNumbers) throws IOException {
        logger.info("Received request to /api/orders-dropship/upload-shipping-label/{}", orderId);

        byte[] fileContent = shippingLabel.getBytes();
        String fileName = shippingLabel.getOriginalFilename();

        String response = goldenSneakersClient.uploadShippingLabel(
            orderId, fileContent, fileName, trackingNumbers).block();

        return ResponseEntity.status(HttpStatus.CREATED)
            .contentType(MediaType.APPLICATION_JSON)
            .body(response);
    }

    // ========== Exception Handlers ==========

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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        logger.error("Unexpected error: {}", ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .contentType(MediaType.APPLICATION_JSON)
            .body(new ErrorResponse("internal_error", "An unexpected error occurred: " + ex.getMessage()));
    }

    // ========== Helper Methods ==========

    private ResponseEntity<byte[]> createDownloadResponse(byte[] content, String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(content.length);

        return new ResponseEntity<>(content, headers, HttpStatus.OK);
    }

    public record ErrorResponse(String error, String message) {}
}
