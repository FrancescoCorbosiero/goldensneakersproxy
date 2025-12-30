package it.alpacode.goldensneakersproxy.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class GoldenSneakersService {

    private static final Logger logger = LoggerFactory.getLogger(GoldenSneakersService.class);

    private final GoldenSneakersClient client;

    @Value("${goldensneakers.markup-percentage:47}")
    private double markupPercentage;

    @Value("${goldensneakers.vat-percentage:0}")
    private double vatPercentage;

    public GoldenSneakersService(GoldenSneakersClient client) {
        this.client = client;
    }

    // ========== Assortment Endpoints ==========

    public Mono<String> getAssortment(Map<String, String> queryParams) {
        return client.fetchAssortment(applyMarkupParams(queryParams));
    }

    public Mono<String> getAssortmentById(String id, Map<String, String> queryParams) {
        return client.fetchAssortmentById(id, applyMarkupParams(queryParams));
    }

    // ========== Assortment Flat Endpoints ==========

    public Mono<String> getAssortmentFlat(Map<String, String> queryParams) {
        return client.fetchAssortmentFlat(applyMarkupParams(queryParams));
    }

    public Mono<String> getAssortmentFlatById(String id, Map<String, String> queryParams) {
        return client.fetchAssortmentFlatById(id, applyMarkupParams(queryParams));
    }

    // ========== Assortment Size Endpoint ==========

    public Mono<String> getAssortmentSize(Map<String, String> queryParams) {
        return client.fetchAssortmentSize(applyMarkupParams(queryParams));
    }

    // ========== SKU Search Endpoint ==========

    public Mono<String> searchSku(Map<String, String> queryParams) {
        // SKU search doesn't have price fields, pass through as-is
        return client.searchSku(queryParams);
    }

    // ========== Orders Dropship Endpoints ==========

    public Mono<String> createDropshipOrder(String jsonBody) {
        return client.createDropshipOrder(jsonBody);
    }

    public Mono<String> getOrderDetails(String orderId) {
        return client.getOrderDetails(orderId);
    }

    public Mono<String> getPackageDetails(String packageId) {
        return client.getPackageDetails(packageId);
    }

    public Mono<String> uploadShippingLabel(String orderId, MultipartFile file, String trackingNumbers)
            throws IOException {
        byte[] fileContent = file.getBytes();
        String fileName = file.getOriginalFilename();
        return client.uploadShippingLabel(orderId, fileContent, fileName, trackingNumbers);
    }

    // ========== Price Markup Logic ==========

    /**
     * Apply configured markup and VAT parameters to the query params.
     * Only adds params if not already provided by the caller.
     */
    private Map<String, String> applyMarkupParams(Map<String, String> originalParams) {
        Map<String, String> params = new HashMap<>(originalParams);

        // Only apply default markup if not already specified
        if (!params.containsKey("markup_percentage") && markupPercentage > 0) {
            params.put("markup_percentage", String.valueOf(markupPercentage));
            logger.debug("Applied default markup_percentage: {}%", markupPercentage);
        }

        // Only apply default VAT if not already specified
        if (!params.containsKey("vat_percentage") && vatPercentage > 0) {
            params.put("vat_percentage", String.valueOf(vatPercentage));
            logger.debug("Applied default vat_percentage: {}%", vatPercentage);
        }

        return params;
    }

    // ========== Getters for configuration ==========

    public double getMarkupPercentage() {
        return markupPercentage;
    }

    public double getVatPercentage() {
        return vatPercentage;
    }
}
