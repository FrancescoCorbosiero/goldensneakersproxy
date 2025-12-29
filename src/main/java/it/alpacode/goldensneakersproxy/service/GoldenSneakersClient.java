package it.alpacode.goldensneakersproxy.service;

import it.alpacode.goldensneakersproxy.config.GoldenSneakersConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class GoldenSneakersClient {

    private static final Logger logger = LoggerFactory.getLogger(GoldenSneakersClient.class);

    private final WebClient webClient;
    private final GoldenSneakersConfig config;

    public GoldenSneakersClient(GoldenSneakersConfig config) {
        this.config = config;
        this.webClient = WebClient.builder()
            .codecs(configurer -> configurer
                .defaultCodecs()
                .maxInMemorySize(50 * 1024 * 1024)) // 50MB for large responses
            .build();
    }

    /**
     * Generic GET request to any endpoint
     */
    public Mono<String> get(String endpoint, Map<String, String> queryParams) {
        String baseUrl = config.getBaseUrl();

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(baseUrl + endpoint);

        // Add all query parameters from the original request
        queryParams.forEach(uriBuilder::queryParam);

        String uri = uriBuilder.build().toUriString();

        logger.debug("GET request to: {}", uri);

        return webClient.get()
            .uri(uri)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + config.getJwtToken())
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                response -> response.bodyToMono(String.class)
                    .flatMap(body -> {
                        logger.error("GoldenSneakers API error: {} - {}", response.statusCode(), body);
                        return Mono.error(new GoldenSneakersApiException(
                            body,
                            response.statusCode().value()
                        ));
                    })
            )
            .bodyToMono(String.class)
            .doOnSuccess(response -> logger.debug("Successfully fetched from {}, response length: {} chars",
                endpoint, response != null ? response.length() : 0))
            .doOnError(error -> logger.error("Error fetching from {}: {}", endpoint, error.getMessage()));
    }

    /**
     * Generic POST request with JSON body
     */
    public Mono<String> post(String endpoint, String jsonBody) {
        String baseUrl = config.getBaseUrl();
        String uri = baseUrl + endpoint;

        logger.debug("POST request to: {} with body length: {}", uri, jsonBody != null ? jsonBody.length() : 0);

        return webClient.post()
            .uri(uri)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + config.getJwtToken())
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .bodyValue(jsonBody)
            .retrieve()
            .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                response -> response.bodyToMono(String.class)
                    .flatMap(body -> {
                        logger.error("GoldenSneakers API error: {} - {}", response.statusCode(), body);
                        return Mono.error(new GoldenSneakersApiException(
                            body,
                            response.statusCode().value()
                        ));
                    })
            )
            .bodyToMono(String.class)
            .doOnSuccess(response -> logger.debug("Successfully posted to {}, response length: {} chars",
                endpoint, response != null ? response.length() : 0))
            .doOnError(error -> logger.error("Error posting to {}: {}", endpoint, error.getMessage()));
    }

    /**
     * Multipart POST request for file uploads
     */
    public Mono<String> postMultipart(String endpoint, byte[] fileContent, String fileName,
                                       String trackingNumbers) {
        String baseUrl = config.getBaseUrl();
        String uri = baseUrl + endpoint;

        logger.debug("POST multipart request to: {} with file: {}", uri, fileName);

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("shipping_label", fileContent)
            .filename(fileName)
            .contentType(MediaType.APPLICATION_OCTET_STREAM);
        builder.part("tracking_numbers", trackingNumbers);

        return webClient.post()
            .uri(uri)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + config.getJwtToken())
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .body(BodyInserters.fromMultipartData(builder.build()))
            .retrieve()
            .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                response -> response.bodyToMono(String.class)
                    .flatMap(body -> {
                        logger.error("GoldenSneakers API error: {} - {}", response.statusCode(), body);
                        return Mono.error(new GoldenSneakersApiException(
                            body,
                            response.statusCode().value()
                        ));
                    })
            )
            .bodyToMono(String.class)
            .doOnSuccess(response -> logger.debug("Successfully uploaded to {}", endpoint))
            .doOnError(error -> logger.error("Error uploading to {}: {}", endpoint, error.getMessage()));
    }

    // ========== Assortment Endpoints ==========

    public Mono<String> fetchAssortment(Map<String, String> queryParams) {
        return get("/assortment/", queryParams);
    }

    public Mono<String> fetchAssortmentById(String id, Map<String, String> queryParams) {
        return get("/assortment/" + id + "/", queryParams);
    }

    // ========== Assortment Flat Endpoints ==========

    public Mono<String> fetchAssortmentFlat(Map<String, String> queryParams) {
        return get("/assortment-flat/", queryParams);
    }

    public Mono<String> fetchAssortmentFlatById(String id, Map<String, String> queryParams) {
        return get("/assortment-flat/" + id + "/", queryParams);
    }

    // ========== Assortment Size Endpoint ==========

    public Mono<String> fetchAssortmentSize(Map<String, String> queryParams) {
        return get("/assortment-size/", queryParams);
    }

    // ========== SKU Search Endpoint ==========

    public Mono<String> searchSku(Map<String, String> queryParams) {
        return get("/sku-search/", queryParams);
    }

    // ========== Orders Dropship Endpoints ==========

    public Mono<String> createDropshipOrder(String jsonBody) {
        return post("/orders-dropship/create-order/", jsonBody);
    }

    public Mono<String> getOrderDetails(String orderId) {
        return get("/orders-dropship/order-details/" + orderId + "/", Map.of());
    }

    public Mono<String> getPackageDetails(String packageId) {
        return get("/orders-dropship/package-details/" + packageId + "/", Map.of());
    }

    public Mono<String> uploadShippingLabel(String orderId, byte[] fileContent, String fileName,
                                             String trackingNumbers) {
        return postMultipart("/orders-dropship/upload-shipping-label/" + orderId + "/",
            fileContent, fileName, trackingNumbers);
    }

    public static class GoldenSneakersApiException extends RuntimeException {
        private final int statusCode;

        public GoldenSneakersApiException(String message, int statusCode) {
            super(message);
            this.statusCode = statusCode;
        }

        public int getStatusCode() {
            return statusCode;
        }
    }
}
