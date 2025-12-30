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

/**
 * Pure Passthrough Proxy Controller - 1:1 mapping to Golden Sneakers API.
 * No business logic, no price manipulation. Direct proxy to upstream API.
 */
@RestController
@RequestMapping("/proxy")
public class PassthroughProxyController {

    private static final Logger logger = LoggerFactory.getLogger(PassthroughProxyController.class);

    private final GoldenSneakersClient client;

    public PassthroughProxyController(GoldenSneakersClient client) {
        this.client = client;
    }

    // ========== Assortment Endpoints (passthrough) ==========

    /**
     * GET /proxy/assortment - List all products (passthrough)
     */
    @GetMapping(value = "/assortment", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getAssortment(@RequestParam Map<String, String> queryParams) {
        logger.info("Received passthrough request to /proxy/assortment with params: {}", queryParams);

        String response = client.fetchAssortment(queryParams).block();

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(response);
    }

    /**
     * GET /proxy/assortment/{id} - Get a specific assortment by ID (passthrough)
     */
    @GetMapping(value = "/assortment/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getAssortmentById(
            @PathVariable String id,
            @RequestParam Map<String, String> queryParams) {
        logger.info("Received passthrough request to /proxy/assortment/{} with params: {}", id, queryParams);

        String response = client.fetchAssortmentById(id, queryParams).block();

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(response);
    }

    // ========== Assortment Flat Endpoints (passthrough) ==========

    /**
     * GET /proxy/assortment-flat - List all sizes as individual products (passthrough)
     */
    @GetMapping(value = "/assortment-flat", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getAssortmentFlat(@RequestParam Map<String, String> queryParams) {
        logger.info("Received passthrough request to /proxy/assortment-flat with params: {}", queryParams);

        String response = client.fetchAssortmentFlat(queryParams).block();

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(response);
    }

    /**
     * GET /proxy/assortment-flat/{id} - Get a specific flat size by ID (passthrough)
     */
    @GetMapping(value = "/assortment-flat/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getAssortmentFlatById(
            @PathVariable String id,
            @RequestParam Map<String, String> queryParams) {
        logger.info("Received passthrough request to /proxy/assortment-flat/{} with params: {}", id, queryParams);

        String response = client.fetchAssortmentFlatById(id, queryParams).block();

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(response);
    }

    // ========== Assortment Size Endpoint (passthrough) ==========

    /**
     * GET /proxy/assortment-size - Retrieve product info by size ID or barcode (passthrough)
     */
    @GetMapping(value = "/assortment-size", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getAssortmentSize(@RequestParam Map<String, String> queryParams) {
        logger.info("Received passthrough request to /proxy/assortment-size with params: {}", queryParams);

        String response = client.fetchAssortmentSize(queryParams).block();

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(response);
    }

    // ========== SKU Search Endpoint (passthrough) ==========

    /**
     * GET /proxy/sku-search - Search products by SKU or name (passthrough)
     */
    @GetMapping(value = "/sku-search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> searchSku(@RequestParam Map<String, String> queryParams) {
        logger.info("Received passthrough request to /proxy/sku-search with params: {}", queryParams);

        String response = client.searchSku(queryParams).block();

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(response);
    }

    // ========== Orders Dropship Endpoints (passthrough) ==========

    /**
     * POST /proxy/orders-dropship/create-order - Create a dropship order (passthrough)
     */
    @PostMapping(value = "/orders-dropship/create-order",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> createDropshipOrder(@RequestBody String requestBody) {
        logger.info("Received passthrough request to /proxy/orders-dropship/create-order");

        String response = client.createDropshipOrder(requestBody).block();

        return ResponseEntity.status(HttpStatus.CREATED)
            .contentType(MediaType.APPLICATION_JSON)
            .body(response);
    }

    /**
     * GET /proxy/orders-dropship/order-details/{orderId} - Get dropship order details (passthrough)
     */
    @GetMapping(value = "/orders-dropship/order-details/{orderId}",
                produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getOrderDetails(@PathVariable String orderId) {
        logger.info("Received passthrough request to /proxy/orders-dropship/order-details/{}", orderId);

        String response = client.getOrderDetails(orderId).block();

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(response);
    }

    /**
     * GET /proxy/orders-dropship/package-details/{packageId} - Get dropship package details (passthrough)
     */
    @GetMapping(value = "/orders-dropship/package-details/{packageId}",
                produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getPackageDetails(@PathVariable String packageId) {
        logger.info("Received passthrough request to /proxy/orders-dropship/package-details/{}", packageId);

        String response = client.getPackageDetails(packageId).block();

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(response);
    }

    /**
     * POST /proxy/orders-dropship/upload-shipping-label/{orderId} - Upload shipping label (passthrough)
     */
    @PostMapping(value = "/orders-dropship/upload-shipping-label/{orderId}",
                 consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> uploadShippingLabel(
            @PathVariable String orderId,
            @RequestParam("shipping_label") MultipartFile shippingLabel,
            @RequestParam("tracking_numbers") String trackingNumbers) throws IOException {
        logger.info("Received passthrough request to /proxy/orders-dropship/upload-shipping-label/{}", orderId);

        byte[] fileContent = shippingLabel.getBytes();
        String fileName = shippingLabel.getOriginalFilename();

        String response = client.uploadShippingLabel(orderId, fileContent, fileName, trackingNumbers).block();

        return ResponseEntity.status(HttpStatus.CREATED)
            .contentType(MediaType.APPLICATION_JSON)
            .body(response);
    }

    // ========== Download Endpoints (passthrough) ==========

    /**
     * Download endpoint for assortment list (passthrough - no markup)
     */
    @GetMapping(value = "/download/assortment.json", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> downloadAssortment(@RequestParam Map<String, String> queryParams) {
        logger.info("Received passthrough request to /proxy/download/assortment.json with params: {}", queryParams);

        String response = client.fetchAssortment(queryParams).block();
        byte[] content = response != null ? response.getBytes() : new byte[0];

        return createDownloadResponse(content, "assortment.json");
    }

    /**
     * Download endpoint for assortment-flat list (passthrough - no markup)
     */
    @GetMapping(value = "/download/assortment-flat.json", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> downloadAssortmentFlat(@RequestParam Map<String, String> queryParams) {
        logger.info("Received passthrough request to /proxy/download/assortment-flat.json with params: {}", queryParams);

        String response = client.fetchAssortmentFlat(queryParams).block();
        byte[] content = response != null ? response.getBytes() : new byte[0];

        return createDownloadResponse(content, "assortment-flat.json");
    }

    // ========== Helper Methods ==========

    private ResponseEntity<byte[]> createDownloadResponse(byte[] content, String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(content.length);

        return new ResponseEntity<>(content, headers, HttpStatus.OK);
    }
}
