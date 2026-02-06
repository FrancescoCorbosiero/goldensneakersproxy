package it.alpacode.goldensneakersproxy.service;

import it.alpacode.goldensneakersproxy.config.WordPressConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class WordPressClient {

    private static final Logger logger = LoggerFactory.getLogger(WordPressClient.class);

    private final WebClient webClient;
    private final WordPressConfig config;

    public WordPressClient(WordPressConfig config) {
        this.config = config;
        this.webClient = WebClient.builder()
            .codecs(configurer -> configurer
                .defaultCodecs()
                .maxInMemorySize(50 * 1024 * 1024))
            .build();
    }

    private String basicAuth() {
        String credentials = config.getConsumerKey() + ":" + config.getConsumerSecret();
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }

    // ========== WooCommerce REST API ==========

    /**
     * Create a product category via WooCommerce REST API.
     * POST /wp-json/wc/v3/products/categories
     */
    public Mono<String> createCategory(String jsonBody) {
        String uri = config.getBaseUrl() + "/wp-json/wc/v3/products/categories";
        logger.debug("POST category to: {}", uri);

        return webClient.post()
            .uri(uri)
            .header(HttpHeaders.AUTHORIZATION, basicAuth())
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .bodyValue(jsonBody)
            .retrieve()
            .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                response -> response.bodyToMono(String.class)
                    .flatMap(body -> {
                        logger.error("WordPress API error: {} - {}", response.statusCode(), body);
                        return Mono.error(new WordPressApiException(body, response.statusCode().value()));
                    })
            )
            .bodyToMono(String.class);
    }

    /**
     * Create a product tag (used for brands) via WooCommerce REST API.
     * POST /wp-json/wc/v3/products/tags
     *
     * Note: Brands in WooCommerce are typically handled via tags or a custom taxonomy
     * plugin (e.g. Perfect Brands for WooCommerce). This uses tags by default.
     * Override the endpoint in subclass or config if using a custom taxonomy.
     */
    public Mono<String> createBrand(String jsonBody) {
        String uri = config.getBaseUrl() + "/wp-json/wc/v3/products/tags";
        logger.debug("POST brand to: {}", uri);

        return webClient.post()
            .uri(uri)
            .header(HttpHeaders.AUTHORIZATION, basicAuth())
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .bodyValue(jsonBody)
            .retrieve()
            .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                response -> response.bodyToMono(String.class)
                    .flatMap(body -> {
                        logger.error("WordPress API error: {} - {}", response.statusCode(), body);
                        return Mono.error(new WordPressApiException(body, response.statusCode().value()));
                    })
            )
            .bodyToMono(String.class);
    }

    /**
     * Create a product attribute via WooCommerce REST API.
     * POST /wp-json/wc/v3/products/attributes
     */
    public Mono<String> createAttribute(String jsonBody) {
        String uri = config.getBaseUrl() + "/wp-json/wc/v3/products/attributes";
        logger.debug("POST attribute to: {}", uri);

        return webClient.post()
            .uri(uri)
            .header(HttpHeaders.AUTHORIZATION, basicAuth())
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .bodyValue(jsonBody)
            .retrieve()
            .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                response -> response.bodyToMono(String.class)
                    .flatMap(body -> {
                        logger.error("WordPress API error: {} - {}", response.statusCode(), body);
                        return Mono.error(new WordPressApiException(body, response.statusCode().value()));
                    })
            )
            .bodyToMono(String.class);
    }

    // ========== WooCommerce REST API (List/Pull) ==========

    /**
     * List product categories from WooCommerce REST API (single page).
     * GET /wp-json/wc/v3/products/categories?per_page={perPage}&page={page}
     */
    public Mono<String> listCategories(int page, int perPage) {
        return listPage("/wp-json/wc/v3/products/categories", page, perPage);
    }

    /**
     * List product tags (brands) from WooCommerce REST API (single page).
     * GET /wp-json/wc/v3/products/tags?per_page={perPage}&page={page}
     */
    public Mono<String> listBrands(int page, int perPage) {
        return listPage("/wp-json/wc/v3/products/tags", page, perPage);
    }

    /**
     * List product attributes from WooCommerce REST API (single page).
     * GET /wp-json/wc/v3/products/attributes?per_page={perPage}&page={page}
     */
    public Mono<String> listAttributes(int page, int perPage) {
        return listPage("/wp-json/wc/v3/products/attributes", page, perPage);
    }

    private Mono<String> listPage(String path, int page, int perPage) {
        String uri = config.getBaseUrl() + path + "?per_page=" + perPage + "&page=" + page;
        logger.debug("GET {} (page {}, per_page {})", path, page, perPage);

        return webClient.get()
            .uri(uri)
            .header(HttpHeaders.AUTHORIZATION, basicAuth())
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                response -> response.bodyToMono(String.class)
                    .flatMap(body -> {
                        logger.error("WordPress API error: {} - {}", response.statusCode(), body);
                        return Mono.error(new WordPressApiException(body, response.statusCode().value()));
                    })
            )
            .bodyToMono(String.class);
    }

    // ========== WordPress REST API (Media) ==========

    /**
     * Upload media to WordPress by providing a source URL.
     * WordPress will sideload the image from the URL.
     * POST /wp-json/wp/v2/media with image binary downloaded from sourceUrl.
     */
    public Mono<String> uploadMediaFromUrl(byte[] imageData, String filename, String mimeType) {
        String uri = config.getBaseUrl() + "/wp-json/wp/v2/media";
        logger.debug("POST media to: {} (filename: {})", uri, filename);

        return webClient.post()
            .uri(uri)
            .header(HttpHeaders.AUTHORIZATION, basicAuth())
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.parseMediaType(mimeType))
            .body(BodyInserters.fromValue(imageData))
            .retrieve()
            .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                response -> response.bodyToMono(String.class)
                    .flatMap(body -> {
                        logger.error("WordPress API error: {} - {}", response.statusCode(), body);
                        return Mono.error(new WordPressApiException(body, response.statusCode().value()));
                    })
            )
            .bodyToMono(String.class);
    }

    /**
     * Download raw bytes from a URL (used to fetch images before uploading to WP).
     */
    public Mono<byte[]> downloadFile(String url) {
        logger.debug("Downloading file from: {}", url);

        return webClient.get()
            .uri(url)
            .retrieve()
            .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                response -> {
                    logger.error("Download error: {} for URL: {}", response.statusCode(), url);
                    return Mono.error(new WordPressApiException(
                        "Failed to download file from: " + url, response.statusCode().value()));
                }
            )
            .bodyToMono(byte[].class);
    }

    // ========== Exception ==========

    public static class WordPressApiException extends RuntimeException {
        private final int statusCode;

        public WordPressApiException(String message, int statusCode) {
            super(message);
            this.statusCode = statusCode;
        }

        public int getStatusCode() {
            return statusCode;
        }
    }
}
