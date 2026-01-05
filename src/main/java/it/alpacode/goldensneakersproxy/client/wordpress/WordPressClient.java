package it.alpacode.goldensneakersproxy.client.wordpress;

import it.alpacode.goldensneakersproxy.client.wordpress.dto.BrandDto;
import it.alpacode.goldensneakersproxy.client.wordpress.dto.TaxonomyCreateRequestDto;
import it.alpacode.goldensneakersproxy.client.wordpress.dto.WpTagDto;
import it.alpacode.goldensneakersproxy.config.ShopProperties;
import it.alpacode.goldensneakersproxy.exception.WooCommerceApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

/**
 * WordPress API client for taxonomy (brands, tags) operations.
 * Uses the WooCommerce REST API for product-related taxonomies.
 */
@Service
public class WordPressClient {

    private static final Logger log = LoggerFactory.getLogger(WordPressClient.class);

    private final WebClient wooCommerceWebClient;
    private final ShopProperties shopProperties;

    public WordPressClient(
            @Qualifier("wooCommerceWebClient") WebClient wooCommerceWebClient,
            ShopProperties shopProperties) {
        this.wooCommerceWebClient = wooCommerceWebClient;
        this.shopProperties = shopProperties;
    }

    /**
     * List all brands from WooCommerce (uses product_brand taxonomy).
     * Compatible with Perfect Brands for WooCommerce and similar plugins.
     */
    public List<BrandDto> listAllBrands(int perPage) {
        List<BrandDto> allBrands = new ArrayList<>();
        int page = 1;
        boolean hasMore = true;

        while (hasMore) {
            final int currentPage = page;

            try {
                List<BrandDto> brands = wooCommerceWebClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/products/brands")
                                .queryParam("per_page", perPage)
                                .queryParam("page", currentPage)
                                .build())
                        .retrieve()
                        .onStatus(HttpStatusCode::isError, resp ->
                                resp.bodyToMono(String.class)
                                        .flatMap(body -> Mono.error(
                                                new WooCommerceApiException("Failed to fetch brands",
                                                        resp.statusCode().value(), body))))
                        .bodyToMono(new ParameterizedTypeReference<List<BrandDto>>() {})
                        .block();

                if (brands != null && !brands.isEmpty()) {
                    allBrands.addAll(brands);
                    page++;
                    if (brands.size() < perPage) {
                        hasMore = false;
                    }
                } else {
                    hasMore = false;
                }
            } catch (WooCommerceApiException e) {
                // Brands endpoint might not exist if plugin not installed
                if (e.getStatusCode() == 404) {
                    log.warn("Brands endpoint not available - brands plugin may not be installed");
                    hasMore = false;
                } else {
                    throw e;
                }
            }
        }

        log.debug("Fetched {} brands from shop", allBrands.size());
        return allBrands;
    }

    /**
     * Create a brand.
     */
    public BrandDto createBrand(TaxonomyCreateRequestDto request) {
        try {
            return wooCommerceWebClient.post()
                    .uri("/products/brands")
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, resp ->
                            resp.bodyToMono(String.class)
                                    .flatMap(body -> Mono.error(
                                            new WooCommerceApiException("Failed to create brand",
                                                    resp.statusCode().value(), body))))
                    .bodyToMono(BrandDto.class)
                    .block();
        } catch (WooCommerceApiException e) {
            if (e.getStatusCode() == 404) {
                log.warn("Brands endpoint not available - brand '{}' not created", request.getName());
                return null;
            }
            throw e;
        }
    }

    /**
     * Create brands in batch by making individual requests.
     * WooCommerce brands API doesn't support batch, so we create one by one.
     */
    public List<BrandDto> createBrandsBatch(List<TaxonomyCreateRequestDto> requests) {
        List<BrandDto> created = new ArrayList<>();

        for (TaxonomyCreateRequestDto request : requests) {
            try {
                BrandDto brand = createBrand(request);
                if (brand != null) {
                    created.add(brand);
                    log.debug("Created brand: {} (ID: {})", brand.getName(), brand.getId());
                }
            } catch (WooCommerceApiException e) {
                // Check if it's a duplicate error (term already exists)
                if (e.getResponseBody() != null && e.getResponseBody().contains("term_exists")) {
                    log.debug("Brand '{}' already exists, skipping", request.getName());
                } else {
                    log.error("Failed to create brand '{}': {}", request.getName(), e.getMessage());
                    throw e;
                }
            }
        }

        log.info("Created {} brands", created.size());
        return created;
    }

    /**
     * List all product tags.
     */
    public List<WpTagDto> listAllTags(int perPage) {
        List<WpTagDto> allTags = new ArrayList<>();
        int page = 1;
        boolean hasMore = true;

        while (hasMore) {
            final int currentPage = page;

            List<WpTagDto> tags = wooCommerceWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/products/tags")
                            .queryParam("per_page", perPage)
                            .queryParam("page", currentPage)
                            .build())
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, resp ->
                            resp.bodyToMono(String.class)
                                    .flatMap(body -> Mono.error(
                                            new WooCommerceApiException("Failed to fetch tags",
                                                    resp.statusCode().value(), body))))
                    .bodyToMono(new ParameterizedTypeReference<List<WpTagDto>>() {})
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
    public WpTagDto createTag(TaxonomyCreateRequestDto request) {
        return wooCommerceWebClient.post()
                .uri("/products/tags")
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(
                                        new WooCommerceApiException("Failed to create tag",
                                                resp.statusCode().value(), body))))
                .bodyToMono(WpTagDto.class)
                .block();
    }

    /**
     * Create tags in batch by making individual requests.
     */
    public List<WpTagDto> createTagsBatch(List<TaxonomyCreateRequestDto> requests) {
        List<WpTagDto> created = new ArrayList<>();

        for (TaxonomyCreateRequestDto request : requests) {
            try {
                WpTagDto tag = createTag(request);
                if (tag != null) {
                    created.add(tag);
                    log.debug("Created tag: {} (ID: {})", tag.getName(), tag.getId());
                }
            } catch (WooCommerceApiException e) {
                // Check if it's a duplicate error
                if (e.getResponseBody() != null && e.getResponseBody().contains("term_exists")) {
                    log.debug("Tag '{}' already exists, skipping", request.getName());
                } else {
                    log.error("Failed to create tag '{}': {}", request.getName(), e.getMessage());
                    throw e;
                }
            }
        }

        log.info("Created {} tags", created.size());
        return created;
    }
}
