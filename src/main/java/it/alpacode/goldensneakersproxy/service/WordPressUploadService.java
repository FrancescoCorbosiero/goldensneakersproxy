package it.alpacode.goldensneakersproxy.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.alpacode.goldensneakersproxy.config.WordPressConfig;
import it.alpacode.goldensneakersproxy.entity.wordpress.WpAttributeLookup;
import it.alpacode.goldensneakersproxy.entity.wordpress.WpBrandLookup;
import it.alpacode.goldensneakersproxy.entity.wordpress.WpCategoryLookup;
import it.alpacode.goldensneakersproxy.entity.wordpress.WpMediaLookup;
import it.alpacode.goldensneakersproxy.repository.WpAttributeLookupRepository;
import it.alpacode.goldensneakersproxy.repository.WpBrandLookupRepository;
import it.alpacode.goldensneakersproxy.repository.WpCategoryLookupRepository;
import it.alpacode.goldensneakersproxy.repository.WpMediaLookupRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class WordPressUploadService {

    private static final Logger logger = LoggerFactory.getLogger(WordPressUploadService.class);

    private final WordPressClient wordPressClient;
    private final WordPressConfig wordPressConfig;
    private final ObjectMapper objectMapper;
    private final WpMediaLookupRepository mediaLookupRepository;
    private final WpCategoryLookupRepository categoryLookupRepository;
    private final WpBrandLookupRepository brandLookupRepository;
    private final WpAttributeLookupRepository attributeLookupRepository;

    public WordPressUploadService(WordPressClient wordPressClient,
                                   WordPressConfig wordPressConfig,
                                   ObjectMapper objectMapper,
                                   WpMediaLookupRepository mediaLookupRepository,
                                   WpCategoryLookupRepository categoryLookupRepository,
                                   WpBrandLookupRepository brandLookupRepository,
                                   WpAttributeLookupRepository attributeLookupRepository) {
        this.wordPressClient = wordPressClient;
        this.wordPressConfig = wordPressConfig;
        this.objectMapper = objectMapper;
        this.mediaLookupRepository = mediaLookupRepository;
        this.categoryLookupRepository = categoryLookupRepository;
        this.brandLookupRepository = brandLookupRepository;
        this.attributeLookupRepository = attributeLookupRepository;
    }

    // ========== CATEGORIES ==========

    /**
     * Upload categories to WordPress. Uses static config values.
     * Skips categories that already exist in the lookup table.
     */
    public List<WpCategoryLookup> uploadCategoriesFromConfig() {
        List<String> categoryNames = wordPressConfig.getCategories();
        logger.info("Uploading {} categories from config", categoryNames.size());
        return uploadCategories(categoryNames);
    }

    /**
     * Upload a list of category names to WordPress.
     * Skips categories that already exist in the lookup table.
     */
    public List<WpCategoryLookup> uploadCategories(List<String> categoryNames) {
        List<WpCategoryLookup> results = new ArrayList<>();

        for (String name : categoryNames) {
            Optional<WpCategoryLookup> existing = categoryLookupRepository.findByName(name);
            if (existing.isPresent()) {
                logger.debug("Category '{}' already exists with WordPress ID: {}", name, existing.get().getWordpressId());
                results.add(existing.get());
                continue;
            }

            try {
                String json = objectMapper.writeValueAsString(Map.of("name", name));
                String response = wordPressClient.createCategory(json).block();
                JsonNode node = objectMapper.readTree(response);

                Integer wpId = node.get("id").asInt();
                String slug = node.has("slug") ? node.get("slug").asText() : null;

                WpCategoryLookup lookup = new WpCategoryLookup(name, slug, wpId);
                WpCategoryLookup saved = categoryLookupRepository.save(lookup);
                results.add(saved);

                logger.info("Category '{}' uploaded -> WordPress ID: {}", name, wpId);
            } catch (JsonProcessingException e) {
                logger.error("Failed to serialize category '{}': {}", name, e.getMessage());
                throw new WordPressUploadException("Failed to serialize category: " + name, e);
            }
        }

        return results;
    }

    // ========== BRANDS ==========

    /**
     * Upload brands to WordPress. Uses static config values.
     * Skips brands that already exist in the lookup table.
     */
    public List<WpBrandLookup> uploadBrandsFromConfig() {
        List<String> brandNames = wordPressConfig.getBrands();
        logger.info("Uploading {} brands from config", brandNames.size());
        return uploadBrands(brandNames);
    }

    /**
     * Upload a list of brand names to WordPress.
     * Skips brands that already exist in the lookup table.
     */
    public List<WpBrandLookup> uploadBrands(List<String> brandNames) {
        List<WpBrandLookup> results = new ArrayList<>();

        for (String name : brandNames) {
            Optional<WpBrandLookup> existing = brandLookupRepository.findByName(name);
            if (existing.isPresent()) {
                logger.debug("Brand '{}' already exists with WordPress ID: {}", name, existing.get().getWordpressId());
                results.add(existing.get());
                continue;
            }

            try {
                String json = objectMapper.writeValueAsString(Map.of("name", name));
                String response = wordPressClient.createBrand(json).block();
                JsonNode node = objectMapper.readTree(response);

                Integer wpId = node.get("id").asInt();
                String slug = node.has("slug") ? node.get("slug").asText() : null;

                WpBrandLookup lookup = new WpBrandLookup(name, slug, wpId);
                WpBrandLookup saved = brandLookupRepository.save(lookup);
                results.add(saved);

                logger.info("Brand '{}' uploaded -> WordPress ID: {}", name, wpId);
            } catch (JsonProcessingException e) {
                logger.error("Failed to serialize brand '{}': {}", name, e.getMessage());
                throw new WordPressUploadException("Failed to serialize brand: " + name, e);
            }
        }

        return results;
    }

    // ========== ATTRIBUTES ==========

    /**
     * Upload attributes to WordPress. Uses static config values.
     * Skips attributes that already exist in the lookup table.
     */
    public List<WpAttributeLookup> uploadAttributesFromConfig() {
        List<String> attributeNames = wordPressConfig.getAttributes();
        logger.info("Uploading {} attributes from config", attributeNames.size());
        return uploadAttributes(attributeNames);
    }

    /**
     * Upload a list of attribute names to WordPress.
     * Skips attributes that already exist in the lookup table.
     */
    public List<WpAttributeLookup> uploadAttributes(List<String> attributeNames) {
        List<WpAttributeLookup> results = new ArrayList<>();

        for (String name : attributeNames) {
            Optional<WpAttributeLookup> existing = attributeLookupRepository.findByName(name);
            if (existing.isPresent()) {
                logger.debug("Attribute '{}' already exists with WordPress ID: {}", name, existing.get().getWordpressId());
                results.add(existing.get());
                continue;
            }

            try {
                String json = objectMapper.writeValueAsString(Map.of(
                    "name", name,
                    "slug", name.toLowerCase().replace(" ", "-"),
                    "type", "select",
                    "has_archives", true
                ));
                String response = wordPressClient.createAttribute(json).block();
                JsonNode node = objectMapper.readTree(response);

                Integer wpId = node.get("id").asInt();
                String slug = node.has("slug") ? node.get("slug").asText() : null;

                WpAttributeLookup lookup = new WpAttributeLookup(name, slug, wpId);
                WpAttributeLookup saved = attributeLookupRepository.save(lookup);
                results.add(saved);

                logger.info("Attribute '{}' uploaded -> WordPress ID: {}", name, wpId);
            } catch (JsonProcessingException e) {
                logger.error("Failed to serialize attribute '{}': {}", name, e.getMessage());
                throw new WordPressUploadException("Failed to serialize attribute: " + name, e);
            }
        }

        return results;
    }

    // ========== MEDIA ==========

    /**
     * Upload a list of image URLs to WordPress media library.
     * Downloads each image, then uploads it to WP.
     * Skips images that already exist in the lookup table.
     */
    public List<WpMediaLookup> uploadMedia(List<String> imageUrls) {
        List<WpMediaLookup> results = new ArrayList<>();

        for (String sourceUrl : imageUrls) {
            Optional<WpMediaLookup> existing = mediaLookupRepository.findBySourceUrl(sourceUrl);
            if (existing.isPresent()) {
                logger.debug("Media '{}' already exists with WordPress ID: {}", sourceUrl, existing.get().getWordpressId());
                results.add(existing.get());
                continue;
            }

            try {
                // Extract filename and mime type from URL
                String filename = extractFilename(sourceUrl);
                String mimeType = guessMimeType(filename);

                // Download the image
                byte[] imageData = wordPressClient.downloadFile(sourceUrl).block();
                if (imageData == null || imageData.length == 0) {
                    logger.warn("Empty response downloading image from: {}", sourceUrl);
                    continue;
                }

                logger.debug("Downloaded {} bytes from: {}", imageData.length, sourceUrl);

                // Upload to WordPress
                String response = wordPressClient.uploadMediaFromUrl(imageData, filename, mimeType).block();
                JsonNode node = objectMapper.readTree(response);

                Integer wpId = node.get("id").asInt();
                String uploadedUrl = null;
                if (node.has("source_url")) {
                    uploadedUrl = node.get("source_url").asText();
                } else if (node.has("guid") && node.get("guid").has("rendered")) {
                    uploadedUrl = node.get("guid").get("rendered").asText();
                }

                WpMediaLookup lookup = new WpMediaLookup(sourceUrl, wpId, uploadedUrl);
                WpMediaLookup saved = mediaLookupRepository.save(lookup);
                results.add(saved);

                logger.info("Media '{}' uploaded -> WordPress ID: {}, URL: {}", sourceUrl, wpId, uploadedUrl);
            } catch (Exception e) {
                logger.error("Failed to upload media '{}': {}", sourceUrl, e.getMessage());
                throw new WordPressUploadException("Failed to upload media: " + sourceUrl, e);
            }
        }

        return results;
    }

    // ========== TAXONOMY SYNC (all from config) ==========

    /**
     * Upload all static taxonomies from config in one call.
     * Order: categories -> brands -> attributes.
     */
    public TaxonomySyncResult uploadAllTaxonomiesFromConfig() {
        logger.info("Starting full taxonomy sync from config");

        List<WpCategoryLookup> categories = uploadCategoriesFromConfig();
        List<WpBrandLookup> brands = uploadBrandsFromConfig();
        List<WpAttributeLookup> attributes = uploadAttributesFromConfig();

        logger.info("Taxonomy sync complete: {} categories, {} brands, {} attributes",
            categories.size(), brands.size(), attributes.size());

        return new TaxonomySyncResult(categories, brands, attributes);
    }

    // ========== LOOKUP QUERIES ==========

    @Transactional(readOnly = true)
    public List<WpCategoryLookup> getAllCategories() {
        return categoryLookupRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<WpBrandLookup> getAllBrands() {
        return brandLookupRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<WpAttributeLookup> getAllAttributes() {
        return attributeLookupRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<WpMediaLookup> getAllMedia() {
        return mediaLookupRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<WpCategoryLookup> getCategoryByName(String name) {
        return categoryLookupRepository.findByName(name);
    }

    @Transactional(readOnly = true)
    public Optional<WpBrandLookup> getBrandByName(String name) {
        return brandLookupRepository.findByName(name);
    }

    @Transactional(readOnly = true)
    public Optional<WpAttributeLookup> getAttributeByName(String name) {
        return attributeLookupRepository.findByName(name);
    }

    @Transactional(readOnly = true)
    public Optional<WpMediaLookup> getMediaBySourceUrl(String sourceUrl) {
        return mediaLookupRepository.findBySourceUrl(sourceUrl);
    }

    // ========== HELPERS ==========

    private String extractFilename(String url) {
        String path = url;
        int queryIndex = path.indexOf('?');
        if (queryIndex > 0) {
            path = path.substring(0, queryIndex);
        }
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash >= 0 && lastSlash < path.length() - 1) {
            return path.substring(lastSlash + 1);
        }
        return "image.jpg";
    }

    private String guessMimeType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        if (lower.endsWith(".bmp")) return "image/bmp";
        return "image/jpeg";
    }

    // ========== RESULT TYPES ==========

    public record TaxonomySyncResult(
        List<WpCategoryLookup> categories,
        List<WpBrandLookup> brands,
        List<WpAttributeLookup> attributes
    ) {}

    // ========== EXCEPTION ==========

    public static class WordPressUploadException extends RuntimeException {
        public WordPressUploadException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
