package it.alpacode.goldensneakersproxy.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Service
public class GoldenSneakersService {

    private static final Logger logger = LoggerFactory.getLogger(GoldenSneakersService.class);

    private final GoldenSneakersClient client;
    private final ObjectMapper objectMapper;

    @Value("${goldensneakers.markup-percentage:47}")
    private double markupPercentage;

    public GoldenSneakersService(GoldenSneakersClient client, ObjectMapper objectMapper) {
        this.client = client;
        this.objectMapper = objectMapper;
    }

    // ========== Assortment Endpoints ==========

    public Mono<String> getAssortment(Map<String, String> queryParams) {
        return client.fetchAssortment(queryParams)
            .map(this::applyPriceMarkupToArray);
    }

    public Mono<String> getAssortmentById(String id, Map<String, String> queryParams) {
        return client.fetchAssortmentById(id, queryParams)
            .map(this::applyPriceMarkupToObject);
    }

    // ========== Assortment Flat Endpoints ==========

    public Mono<String> getAssortmentFlat(Map<String, String> queryParams) {
        return client.fetchAssortmentFlat(queryParams)
            .map(this::applyPriceMarkupToArray);
    }

    public Mono<String> getAssortmentFlatById(String id, Map<String, String> queryParams) {
        return client.fetchAssortmentFlatById(id, queryParams)
            .map(this::applyPriceMarkupToObject);
    }

    // ========== Assortment Size Endpoint ==========

    public Mono<String> getAssortmentSize(Map<String, String> queryParams) {
        return client.fetchAssortmentSize(queryParams)
            .map(this::applyPriceMarkupToObjectWithSizes);
    }

    // ========== SKU Search Endpoint ==========

    public Mono<String> searchSku(Map<String, String> queryParams) {
        // SKU search doesn't contain prices, pass through as-is
        return client.searchSku(queryParams);
    }

    // ========== Orders Dropship Endpoints ==========

    public Mono<String> createDropshipOrder(String jsonBody) {
        // Orders are created with original prices from GoldenSneakers
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
     * Apply markup to an array of products (assortment list, assortment-flat list)
     */
    private String applyPriceMarkupToArray(String jsonResponse) {
        try {
            JsonNode rootNode = objectMapper.readTree(jsonResponse);

            if (rootNode.isArray()) {
                ArrayNode arrayNode = (ArrayNode) rootNode;
                for (JsonNode item : arrayNode) {
                    applyMarkupToProduct((ObjectNode) item);
                }
            }

            return objectMapper.writeValueAsString(rootNode);
        } catch (JsonProcessingException e) {
            logger.error("Error parsing JSON for price markup: {}", e.getMessage());
            return jsonResponse; // Return original on error
        }
    }

    /**
     * Apply markup to a single product object
     */
    private String applyPriceMarkupToObject(String jsonResponse) {
        try {
            JsonNode rootNode = objectMapper.readTree(jsonResponse);

            if (rootNode.isObject()) {
                applyMarkupToProduct((ObjectNode) rootNode);
            }

            return objectMapper.writeValueAsString(rootNode);
        } catch (JsonProcessingException e) {
            logger.error("Error parsing JSON for price markup: {}", e.getMessage());
            return jsonResponse;
        }
    }

    /**
     * Apply markup to object with nested sizes array (AssortmentWithSize)
     */
    private String applyPriceMarkupToObjectWithSizes(String jsonResponse) {
        try {
            JsonNode rootNode = objectMapper.readTree(jsonResponse);

            if (rootNode.isObject()) {
                ObjectNode objectNode = (ObjectNode) rootNode;

                // Apply markup to sizes array if present
                if (objectNode.has("sizes") && objectNode.get("sizes").isArray()) {
                    ArrayNode sizesArray = (ArrayNode) objectNode.get("sizes");
                    for (JsonNode size : sizesArray) {
                        if (size.isObject()) {
                            applyMarkupToSize((ObjectNode) size);
                        }
                    }
                }
            }

            return objectMapper.writeValueAsString(rootNode);
        } catch (JsonProcessingException e) {
            logger.error("Error parsing JSON for price markup: {}", e.getMessage());
            return jsonResponse;
        }
    }

    /**
     * Apply markup to a product node (handles both flat and nested structures)
     */
    private void applyMarkupToProduct(ObjectNode product) {
        // Apply markup to direct price fields
        applyMarkupToField(product, "offer_price");
        applyMarkupToField(product, "presented_price");

        // Handle nested sizes array (for Assortment with embedded sizes)
        if (product.has("sizes") && product.get("sizes").isArray()) {
            ArrayNode sizesArray = (ArrayNode) product.get("sizes");
            for (JsonNode size : sizesArray) {
                if (size.isObject()) {
                    applyMarkupToSize((ObjectNode) size);
                }
            }
        }
    }

    /**
     * Apply markup to a size node
     */
    private void applyMarkupToSize(ObjectNode size) {
        applyMarkupToField(size, "offer_price");
        applyMarkupToField(size, "presented_price");
    }

    /**
     * Apply markup percentage to a specific price field
     */
    private void applyMarkupToField(ObjectNode node, String fieldName) {
        if (!node.has(fieldName) || node.get(fieldName).isNull()) {
            return;
        }

        try {
            JsonNode priceNode = node.get(fieldName);
            BigDecimal originalPrice;

            if (priceNode.isNumber()) {
                originalPrice = priceNode.decimalValue();
            } else if (priceNode.isTextual()) {
                String priceText = priceNode.asText().trim();
                if (priceText.isEmpty()) {
                    return;
                }
                originalPrice = new BigDecimal(priceText);
            } else {
                return;
            }

            // Calculate markup: newPrice = originalPrice * (1 + markupPercentage/100)
            BigDecimal multiplier = BigDecimal.ONE.add(
                BigDecimal.valueOf(markupPercentage).divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
            );
            BigDecimal newPrice = originalPrice.multiply(multiplier)
                .setScale(2, RoundingMode.HALF_UP);

            // Preserve original format (string or number)
            if (priceNode.isTextual()) {
                node.put(fieldName, newPrice.toString());
            } else {
                node.put(fieldName, newPrice);
            }

            logger.trace("Applied {}% markup to {}: {} -> {}",
                markupPercentage, fieldName, originalPrice, newPrice);

        } catch (NumberFormatException e) {
            logger.warn("Could not parse price field '{}': {}", fieldName, e.getMessage());
        }
    }

    // ========== Getters for configuration ==========

    public double getMarkupPercentage() {
        return markupPercentage;
    }
}
