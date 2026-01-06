package it.alpacode.goldensneakersproxy.client.woocommerce;

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
 * Simple and focused on bulk sync.
 */
@Service
public class WooCommerceClient {

    private static final Logger log = LoggerFactory.getLogger(WooCommerceClient.class);

    private final WebClient webClient;
    private final ShopProperties shopProperties;

    public WooCommerceClient(
            @Qualifier("wooCommerceWebClient") WebClient webClient,
            ShopProperties shopProperties) {
        this.webClient = webClient;
        this.shopProperties = shopProperties;
    }

    /**
     * Fetch all products indexed by SKU.
     */
    public Map<String, ProductDto> fetchAllProductsBySku() {
        List<ProductDto> allProducts = new ArrayList<>();
        int page = 1;
        boolean hasMore = true;

        while (hasMore) {
            log.debug("Fetching products page {}", page);
            final int currentPage = page;

            List<ProductDto> products = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/products")
                            .queryParam("per_page", 100)
                            .queryParam("page", currentPage)
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
                if (products.size() < 100) {
                    hasMore = false;
                }
            } else {
                hasMore = false;
            }
        }

        log.info("Fetched {} products", allProducts.size());

        return allProducts.stream()
                .filter(p -> p.getSku() != null && !p.getSku().isEmpty())
                .collect(Collectors.toMap(ProductDto::getSku, p -> p, (a, b) -> a));
    }

    /**
     * Create products in batch.
     */
    public List<ProductDto> createProductsBatch(List<ProductCreateRequestDto> requests) {
        if (requests.isEmpty()) {
            return Collections.emptyList();
        }

        List<ProductDto> allCreated = new ArrayList<>();
        List<List<ProductCreateRequestDto>> chunks = partition(requests, batchSize());

        for (int i = 0; i < chunks.size(); i++) {
            List<ProductCreateRequestDto> chunk = chunks.get(i);
            log.info("Creating batch {}/{} ({} products)", i + 1, chunks.size(), chunk.size());

            BatchRequestDto<ProductCreateRequestDto> batch = BatchRequestDto.forCreate(chunk);

            BatchResponseDto<ProductDto> response = webClient.post()
                    .uri("/products/batch")
                    .bodyValue(batch)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, resp ->
                            resp.bodyToMono(String.class)
                                    .flatMap(body -> Mono.error(
                                            new WooCommerceApiException("Failed to create products",
                                                    resp.statusCode().value(), body))))
                    .bodyToMono(new ParameterizedTypeReference<BatchResponseDto<ProductDto>>() {})
                    .block();

            if (response != null && response.getCreate() != null) {
                allCreated.addAll(response.getCreate());
            }

            if (i < chunks.size() - 1) {
                sleep(rateLimitDelay());
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
        List<List<ProductUpdateRequestDto>> chunks = partition(updates, batchSize());

        for (int i = 0; i < chunks.size(); i++) {
            List<ProductUpdateRequestDto> chunk = chunks.get(i);
            log.info("Updating batch {}/{} ({} products)", i + 1, chunks.size(), chunk.size());

            BatchRequestDto<ProductUpdateRequestDto> batch = BatchRequestDto.forUpdate(chunk);

            BatchResponseDto<ProductDto> response = webClient.post()
                    .uri("/products/batch")
                    .bodyValue(batch)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, resp ->
                            resp.bodyToMono(String.class)
                                    .flatMap(body -> Mono.error(
                                            new WooCommerceApiException("Failed to update products",
                                                    resp.statusCode().value(), body))))
                    .bodyToMono(new ParameterizedTypeReference<BatchResponseDto<ProductDto>>() {})
                    .block();

            if (response != null && response.getUpdate() != null) {
                allUpdated.addAll(response.getUpdate());
            }

            if (i < chunks.size() - 1) {
                sleep(rateLimitDelay());
            }
        }

        return allUpdated;
    }

    /**
     * Mark products out of stock.
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

        List<List<ProductUpdateRequestDto>> chunks = partition(updates, batchSize());

        for (int i = 0; i < chunks.size(); i++) {
            List<ProductUpdateRequestDto> chunk = chunks.get(i);
            log.info("Marking out of stock batch {}/{} ({} products)", i + 1, chunks.size(), chunk.size());

            BatchRequestDto<ProductUpdateRequestDto> batch = BatchRequestDto.forUpdate(chunk);

            webClient.post()
                    .uri("/products/batch")
                    .bodyValue(batch)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, resp ->
                            resp.bodyToMono(String.class)
                                    .flatMap(body -> Mono.error(
                                            new WooCommerceApiException("Failed to mark out of stock",
                                                    resp.statusCode().value(), body))))
                    .bodyToMono(new ParameterizedTypeReference<BatchResponseDto<ProductDto>>() {})
                    .block();

            if (i < chunks.size() - 1) {
                sleep(rateLimitDelay());
            }
        }

        log.info("Marked {} products out of stock", productIds.size());
    }

    /**
     * Upsert variations for a product.
     */
    public List<VariationDto> upsertVariations(Long productId, List<CatalogVariation> variations) {
        if (variations == null || variations.isEmpty()) {
            return Collections.emptyList();
        }

        // Fetch existing
        List<VariationDto> existing = webClient.get()
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

        // Map by size
        Map<String, VariationDto> existingMap = new HashMap<>();
        if (existing != null) {
            for (VariationDto v : existing) {
                if (v.getAttributes() != null && !v.getAttributes().isEmpty()) {
                    String size = v.getAttributes().get(0).getOption();
                    if (size != null) existingMap.put(size, v);
                }
            }
        }

        // Build create/update lists
        List<VariationCreateRequestDto> toCreate = new ArrayList<>();
        List<VariationUpdateRequestDto> toUpdate = new ArrayList<>();

        for (CatalogVariation v : variations) {
            String stockStatus = v.getStockQuantity() != null && v.getStockQuantity() > 0 ? "instock" : "outofstock";

            if (existingMap.containsKey(v.getSize())) {
                VariationDto ex = existingMap.get(v.getSize());
                VariationUpdateRequestDto upd = new VariationUpdateRequestDto();
                upd.setId(ex.getId());
                upd.setRegularPrice(v.getRegularPrice());
                upd.setSalePrice(v.getSalePrice());
                upd.setStockQuantity(v.getStockQuantity());
                upd.setStockStatus(stockStatus);
                toUpdate.add(upd);
            } else {
                VariationCreateRequestDto cr = new VariationCreateRequestDto();
                cr.setSku(v.getSku());
                cr.setRegularPrice(v.getRegularPrice());
                cr.setSalePrice(v.getSalePrice());
                cr.setStockQuantity(v.getStockQuantity());
                cr.setManageStock(true);
                cr.setStockStatus(stockStatus);
                cr.setAttributes(List.of(new VariationAttributeDto("Taglie", v.getSize())));
                if (v.getImageUrl() != null) {
                    cr.setImage(new ImageDto(v.getImageUrl()));
                }
                toCreate.add(cr);
            }
        }

        List<VariationDto> results = new ArrayList<>();

        if (!toCreate.isEmpty() || !toUpdate.isEmpty()) {
            Map<String, Object> batch = new HashMap<>();
            if (!toCreate.isEmpty()) batch.put("create", toCreate);
            if (!toUpdate.isEmpty()) batch.put("update", toUpdate);

            Map<String, List<VariationDto>> response = webClient.post()
                    .uri("/products/{productId}/variations/batch", productId)
                    .bodyValue(batch)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, resp ->
                            resp.bodyToMono(String.class)
                                    .flatMap(body -> Mono.error(
                                            new WooCommerceApiException("Failed to upsert variations",
                                                    resp.statusCode().value(), body))))
                    .bodyToMono(new ParameterizedTypeReference<Map<String, List<VariationDto>>>() {})
                    .block();

            if (response != null) {
                if (response.get("create") != null) results.addAll(response.get("create"));
                if (response.get("update") != null) results.addAll(response.get("update"));
            }
        }

        return results;
    }

    private int batchSize() {
        return shopProperties.getWoocommerce().getBatchSize();
    }

    private int rateLimitDelay() {
        return shopProperties.getWoocommerce().getRateLimitDelayMs();
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
