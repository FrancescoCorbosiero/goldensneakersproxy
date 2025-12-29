package it.alpacode.goldensneakersproxy.service;

import it.alpacode.goldensneakersproxy.config.GoldenSneakersConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
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

    public Mono<String> fetchAssortment(Map<String, String> queryParams) {
        String baseUrl = config.getApiUrl();

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(baseUrl);

        // Add all query parameters from the original request
        queryParams.forEach(uriBuilder::queryParam);

        String uri = uriBuilder.build().toUriString();

        logger.debug("Fetching assortment from: {}", uri);

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
                            "GoldenSneakers API returned: " + response.statusCode() + " - " + body,
                            response.statusCode().value()
                        ));
                    })
            )
            .bodyToMono(String.class)
            .doOnSuccess(response -> logger.debug("Successfully fetched assortment, response length: {} chars",
                response != null ? response.length() : 0))
            .doOnError(error -> logger.error("Error fetching assortment: {}", error.getMessage()));
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
