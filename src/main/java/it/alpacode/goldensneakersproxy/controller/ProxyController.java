package it.alpacode.goldensneakersproxy.controller;

import it.alpacode.goldensneakersproxy.service.GoldenSneakersClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ProxyController {

    private static final Logger logger = LoggerFactory.getLogger(ProxyController.class);

    private final GoldenSneakersClient goldenSneakersClient;

    public ProxyController(GoldenSneakersClient goldenSneakersClient) {
        this.goldenSneakersClient = goldenSneakersClient;
    }

    /**
     * REST JSON Proxy endpoint - proxies requests to GoldenSneakers API
     * Returns JSON response as application/json
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
     * File Download endpoint - returns JSON as downloadable file
     * Useful for Import Wizard Pro's file-based import
     */
    @GetMapping(value = "/download/assortment.json", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> downloadAssortment(@RequestParam Map<String, String> queryParams) {
        logger.info("Received request to /download/assortment.json with params: {}", queryParams);

        String response = goldenSneakersClient.fetchAssortment(queryParams).block();

        byte[] content = response != null ? response.getBytes() : new byte[0];

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "assortment.json");
        headers.setContentLength(content.length);

        return new ResponseEntity<>(content, headers, HttpStatus.OK);
    }

    @ExceptionHandler(GoldenSneakersClient.GoldenSneakersApiException.class)
    public ResponseEntity<ErrorResponse> handleGoldenSneakersApiException(GoldenSneakersClient.GoldenSneakersApiException ex) {
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

    public record ErrorResponse(String error, String message) {}
}
