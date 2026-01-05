package it.alpacode.goldensneakersproxy.client.woocommerce;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.alpacode.goldensneakersproxy.client.woocommerce.dto.*;
import it.alpacode.goldensneakersproxy.config.ShopProperties;
import it.alpacode.goldensneakersproxy.exception.WooCommerceApiException;
import it.alpacode.goldensneakersproxy.model.CatalogVariation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

/**
 * WooCommerce API client for batch operations.
 * All operations are bulk-focused for optimal performance.
 */
@Service
public class WooCommerceClient {

    private static final Logger log = LoggerFactory.getLogger(WooCommerceClient.class);

    private final WebClient webClient;
    private final ShopProperties shopProperties;
    private final ObjectMapper objectMapper;

    public WooCommerceClient(
            @Qualifier("wooCommerceWebClient") WebClient webClient,
            ShopProperties shopProperties,
            ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.shopProperties = shopProperties;
        this.objectMapper = objectMapper;
    }

    /**
     * Fetch ALL products indexed by SKU.
     */
    public Map<String, ProductDto> fetchAllProductsBySku() {
        List<ProductDto> allProducts = new ArrayList<>();
        int page = 1;
        boolean hasMore = true;
        int perPage = 100;

        while (hasMore) {
            log.debug("Fetching products page {}", page);

            List<ProductDto> products = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/products")
                            .queryParam("per_page", perPage)
                            .queryParam("page", page)
                            .queryParam("status", "any")
                            .build())
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response ->
                            response.bodyToMono(String.class)
                                    .flatMap(body -> Mono.error(
                                            new WooCommerceApiException("Failed to fetch products",
                                                    response.statusCode().value(), body))))
                    .bodyToMono(new ParameterizedTypeReference<List<ProductDto>>() {})
                    .block();

            if (products != null && !products.isEmpty()) {
                allProducts.addAll(products);
                page++;
                if (products.size() < perPage) {
                    hasMore = false;
                }
            } else {
                hasMore = false;
            }
        }

        log.info("Fetched {} products from shop", allProducts.size());

        return allProducts.stream()
                .filter(p -> p.getSku() != null && !p.getSku().isEmpty())
                .collect(Collectors.toMap(
                        ProductDto::getSku,
                        p -> p,
                        (existing, replacement) -> existing // Keep first in case of duplicates
                ));
    }

    /**
     * Create products in batch (chunked by batch size).
     */
    public List<ProductDto> createProductsBatch(List<ProductCreateRequestDto> requests) {
        if (requests.isEmpty()) {
            return Collections.emptyList();
        }

        List<ProductDto> allCreated = new ArrayList<>();
        List<List<ProductCreateRequestDto>> chunks = partition(requests, shopProperties.getWoocommerce().getBatchSize());

        for (int i = 0; i < chunks.size(); i++) {
            List<ProductCreateRequestDto> chunk = chunks.get(i);
            log.info("Creating products batch {}/{} ({} products)", i + 1, chunks.size(), chunk.size());

            BatchRequestDto<ProductCreateRequestDto> batchRequest = BatchRequestDto.forCreate(chunk);

            BatchResponseDto<ProductDto> response = webClient.post()
                    .uri("/products/batch")
                    .bodyValue(batchRequest)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, resp ->
                            resp.bodyToMono(String.class)
                                    .flatMap(body -> Mono.error(
                                            new WooCommerceApiException("Failed to create products batch",
                                                    resp.statusCode().value(), body))))
                    .bodyToMono(new ParameterizedTypeReference<BatchResponseDto<ProductDto>>() {})
                    .block();

            if (response != null && response.getCreate() != null) {
                allCreated.addAll(response.getCreate());
                log.info("Batch created {} products", response.getCreate().size());
            }

            // Rate limiting between batches
            if (i < chunks.size() - 1) {
                sleep(shopProperties.getSync().getRateLimitDelayMs());
            }
        }

        return allCreated;
    }

    /**
     * Update products in batch.
     */
    public List<ProductDto> updateProductsBatch(List<ProductUpdateRequestDto> updates) {
        if (updates.isEmpty()) {
            return Collections.emptyList();
        }

        List<ProductDto> allUpdated = new ArrayList<>();
        List<List<ProductUpdateRequestDto>> chunks = partition(updates, shopProperties.getWoocommerce().getBatchSize());

        for (int i = 0; i < chunks.size(); i++) {
            List<ProductUpdateRequestDto> chunk = chunks.get(i);
            log.info("Updating products batch {}/{} ({} products)", i + 1, chunks.size(), chunk.size());

            BatchRequestDto<ProductUpdateRequestDto> batchRequest = BatchRequestDto.forUpdate(chunk);

            BatchResponseDto<ProductDto> response = webClient.post()
                    .uri("/products/batch")
                    .bodyValue(batchRequest)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, resp ->
                            resp.bodyToMono(String.class)
                                    .flatMap(body -> Mono.error(
                                            new WooCommerceApiException("Failed to update products batch",
                                                    resp.statusCode().value(), body))))
                    .bodyToMono(new ParameterizedTypeReference<BatchResponseDto<ProductDto>>() {})
                    .block();

            if (response != null && response.getUpdate() != null) {
                allUpdated.addAll(response.getUpdate());
                log.info("Batch updated {} products", response.getUpdate().size());
            }

            // Rate limiting between batches
            if (i < chunks.size() - 1) {
                sleep(shopProperties.getSync().getRateLimitDelayMs());
            }
        }

        return allUpdated;
    }

    /**
     * Mark products out of stock in batch.
     */
    public void markOutOfStockBatch(List<Long> productIds) {
        if (productIds.isEmpty()) {
            return;
        }

        List<ProductUpdateRequestDto> updates = productIds.stream()
                .map(id -> {
                    ProductUpdateRequestDto dto = new ProductUpdateRequestDto();
                    dto.setId(id);
                    dto.setStockStatus("outofstock");
                    return dto;
                })
                .toList();

        List<List<ProductUpdateRequestDto>> chunks = partition(updates, shopProperties.getWoocommerce().getBatchSize());

        for (int i = 0; i < chunks.size(); i++) {
            List<ProductUpdateRequestDto> chunk = chunks.get(i);
            log.info("Marking out of stock batch {}/{} ({} products)", i + 1, chunks.size(), chunk.size());

            BatchRequestDto<ProductUpdateRequestDto> batchRequest = BatchRequestDto.forUpdate(chunk);

            webClient.post()
                    .uri("/products/batch")
                    .bodyValue(batchRequest)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, resp ->
                            resp.bodyToMono(String.class)
                                    .flatMap(body -> Mono.error(
                                            new WooCommerceApiException("Failed to mark products out of stock",
                                                    resp.statusCode().value(), body))))
                    .bodyToMono(new ParameterizedTypeReference<BatchResponseDto<ProductDto>>() {})
                    .block();

            // Rate limiting between batches
            if (i < chunks.size() - 1) {
                sleep(shopProperties.getSync().getRateLimitDelayMs());
            }
        }

        log.info("Marked {} products out of stock", productIds.size());
    }

    /**
     * Delete products in batch (for rollback).
     */
    public void deleteProductsBatch(List<Long> productIds) {
        if (productIds.isEmpty()) {
            return;
        }

        List<List<Long>> chunks = partition(productIds, shopProperties.getWoocommerce().getBatchSize());

        for (int i = 0; i < chunks.size(); i++) {
            List<Long> chunk = chunks.get(i);
            log.info("Deleting products batch {}/{} ({} products)", i + 1, chunks.size(), chunk.size());

            BatchRequestDto<Object> batchRequest = BatchRequestDto.forDelete(chunk);

            webClient.post()
                    .uri("/products/batch")
                    .bodyValue(batchRequest)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, resp ->
                            resp.bodyToMono(String.class)
                                    .flatMap(body -> Mono.error(
                                            new WooCommerceApiException("Failed to delete products batch",
                                                    resp.statusCode().value(), body))))
                    .bodyToMono(new ParameterizedTypeReference<BatchResponseDto<ProductDto>>() {})
                    .block();

            // Rate limiting between batches
            if (i < chunks.size() - 1) {
                sleep(shopProperties.getSync().getRateLimitDelayMs());
            }
        }

        log.info("Deleted {} products (rollback)", productIds.size());
    }

    /**
     * Upsert variations for a product.
     * Creates new variations and updates existing ones.
     */
    public List<VariationDto> upsertVariations(Long productId, List<CatalogVariation> variations) {
        if (variations == null || variations.isEmpty()) {
            return Collections.emptyList();
        }

        // Fetch existing variations
        List<VariationDto> existingVariations = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/products/{productId}/variations")
                        .queryParam("per_page", 100)
                        .build(productId))
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(
                                        new WooCommerceApiException("Failed to fetch variations",
                                                resp.statusCode().value(), body))))
                .bodyToMono(new ParameterizedTypeReference<List<VariationDto>>() {})
                .block();

        // Map existing by size attribute
        Map<String, VariationDto> existingMap = new HashMap<>();
        if (existingVariations != null) {
            for (VariationDto v : existingVariations) {
                if (v.getAttributes() != null && !v.getAttributes().isEmpty()) {
                    String sizeOption = v.getAttributes().get(0).getOption();
                    if (sizeOption != null) {
                        existingMap.put(sizeOption, v);
                    }
                }
            }
        }

        // Separate into create and update
        List<VariationCreateRequestDto> toCreate = new ArrayList<>();
        List<VariationUpdateRequestDto> toUpdate = new ArrayList<>();

        for (CatalogVariation v : variations) {
            if (existingMap.containsKey(v.getSize())) {
                // Update existing
                VariationDto existing = existingMap.get(v.getSize());
                VariationUpdateRequestDto update = new VariationUpdateRequestDto();
                update.setId(existing.getId());
                update.setRegularPrice(v.getRegularPrice());
                update.setSalePrice(v.getSalePrice());
                update.setStockQuantity(v.getStockQuantity());
                update.setStockStatus(v.getStockQuantity() != null && v.getStockQuantity() > 0 ? "instock" : "outofstock");
                toUpdate.add(update);
            } else {
                // Create new
                VariationCreateRequestDto create = new VariationCreateRequestDto();
                create.setSku(v.getSku());
                create.setRegularPrice(v.getRegularPrice());
                create.setSalePrice(v.getSalePrice());
                create.setStockQuantity(v.getStockQuantity());
                create.setManageStock(true);
                create.setStockStatus(v.getStockQuantity() != null && v.getStockQuantity() > 0 ? "instock" : "outofstock");
                create.setAttributes(List.of(new VariationAttributeDto("Taglie", v.getSize())));
                if (v.getImageUrl() != null) {
                    create.setImage(new ImageDto(v.getImageUrl()));
                }
                toCreate.add(create);
            }
        }

        List<VariationDto> results = new ArrayList<>();

        // Batch create/update variations
        if (!toCreate.isEmpty() || !toUpdate.isEmpty()) {
            Map<String, Object> batchRequest = new HashMap<>();
            if (!toCreate.isEmpty()) {
                batchRequest.put("create", toCreate);
            }
            if (!toUpdate.isEmpty()) {
                batchRequest.put("update", toUpdate);
            }

            Map<String, List<VariationDto>> response = webClient.post()
                    .uri("/products/{productId}/variations/batch", productId)
                    .bodyValue(batchRequest)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, resp ->
                            resp.bodyToMono(String.class)
                                    .flatMap(body -> Mono.error(
                                            new WooCommerceApiException("Failed to upsert variations",
                                                    resp.statusCode().value(), body))))
                    .bodyToMono(new ParameterizedTypeReference<Map<String, List<VariationDto>>>() {})
                    .block();

            if (response != null) {
                if (response.get("create") != null) {
                    results.addAll(response.get("create"));
                }
                if (response.get("update") != null) {
                    results.addAll(response.get("update"));
                }
            }
        }

        log.debug("Product {} - Created {} variations, updated {} variations",
                productId, toCreate.size(), toUpdate.size());

        return results;
    }

    /**
     * List all product tags.
     */
    public List<TagDto> listAllTags() {
        List<TagDto> allTags = new ArrayList<>();
        int page = 1;
        boolean hasMore = true;
        int perPage = 100;

        while (hasMore) {
            List<TagDto> tags = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/products/tags")
                            .queryParam("per_page", perPage)
                            .queryParam("page", page)
                            .build())
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, resp ->
                            resp.bodyToMono(String.class)
                                    .flatMap(body -> Mono.error(
                                            new WooCommerceApiException("Failed to fetch tags",
                                                    resp.statusCode().value(), body))))
                    .bodyToMono(new ParameterizedTypeReference<List<TagDto>>() {})
                    .block();

            if (tags != null && !tags.isEmpty()) {
                allTags.addAll(tags);
                page++;
                if (tags.size() < perPage) {
                    hasMore = false;
                }
            } else {
                hasMore = false;
            }
        }

        log.debug("Fetched {} tags from shop", allTags.size());
        return allTags;
    }

    /**
     * Create a tag.
     */
    public TagDto createTag(String name, String slug) {
        Map<String, String> request = new HashMap<>();
        request.put("name", name);
        request.put("slug", slug);

        return webClient.post()
                .uri("/products/tags")
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(
                                        new WooCommerceApiException("Failed to create tag",
                                                resp.statusCode().value(), body))))
                .bodyToMono(TagDto.class)
                .block();
    }

    private <T> List<List<T>> partition(List<T> list, int size) {
        List<List<T>> result = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            result.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return result;
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
