package it.alpacode.goldensneakersproxy.controller;

import it.alpacode.goldensneakersproxy.entity.wordpress.WpAttributeLookup;
import it.alpacode.goldensneakersproxy.entity.wordpress.WpBrandLookup;
import it.alpacode.goldensneakersproxy.entity.wordpress.WpCategoryLookup;
import it.alpacode.goldensneakersproxy.entity.wordpress.WpMediaLookup;
import it.alpacode.goldensneakersproxy.service.WordPressUploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for WordPress pre-import uploads.
 * Handles uploading taxonomies (categories, brands, attributes) and media to WordPress,
 * populating local lookup tables with the resulting WordPress IDs.
 *
 * Base path: /wp-upload
 */
@RestController
@RequestMapping("/wp-upload")
public class WordPressUploadController {

    private static final Logger logger = LoggerFactory.getLogger(WordPressUploadController.class);

    private final WordPressUploadService uploadService;

    public WordPressUploadController(WordPressUploadService uploadService) {
        this.uploadService = uploadService;
    }

    // ========== UPLOAD TAXONOMIES FROM CONFIG ==========

    /**
     * POST /wp-upload/taxonomies/sync - Upload all static taxonomies from config.
     * Categories, brands, and attributes defined in application.properties.
     */
    @PostMapping(value = "/taxonomies/sync", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WordPressUploadService.TaxonomySyncResult> syncTaxonomiesFromConfig() {
        logger.info("POST /wp-upload/taxonomies/sync - Syncing all taxonomies from config");
        WordPressUploadService.TaxonomySyncResult result = uploadService.uploadAllTaxonomiesFromConfig();
        return ResponseEntity.ok(result);
    }

    // ========== PULL TAXONOMIES FROM WORDPRESS ==========

    /**
     * POST /wp-upload/taxonomies/pull - Pull all existing taxonomies from WordPress
     * into the local lookup tables. Fetches categories, brands (tags), and attributes
     * that were manually created in WordPress UI.
     */
    @PostMapping(value = "/taxonomies/pull", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WordPressUploadService.TaxonomyPullResult> pullTaxonomiesFromWordPress() {
        logger.info("POST /wp-upload/taxonomies/pull - Pulling all taxonomies from WordPress");
        WordPressUploadService.TaxonomyPullResult result = uploadService.pullAllTaxonomiesFromWordPress();
        return ResponseEntity.ok(result);
    }

    /**
     * POST /wp-upload/categories/pull - Pull existing categories from WordPress.
     */
    @PostMapping(value = "/categories/pull", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<WpCategoryLookup>> pullCategoriesFromWordPress() {
        logger.info("POST /wp-upload/categories/pull - Pulling categories from WordPress");
        List<WpCategoryLookup> results = uploadService.pullCategoriesFromWordPress();
        return ResponseEntity.ok(results);
    }

    /**
     * POST /wp-upload/brands/pull - Pull existing brands (tags) from WordPress.
     */
    @PostMapping(value = "/brands/pull", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<WpBrandLookup>> pullBrandsFromWordPress() {
        logger.info("POST /wp-upload/brands/pull - Pulling brands from WordPress");
        List<WpBrandLookup> results = uploadService.pullBrandsFromWordPress();
        return ResponseEntity.ok(results);
    }

    /**
     * POST /wp-upload/attributes/pull - Pull existing attributes from WordPress.
     */
    @PostMapping(value = "/attributes/pull", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<WpAttributeLookup>> pullAttributesFromWordPress() {
        logger.info("POST /wp-upload/attributes/pull - Pulling attributes from WordPress");
        List<WpAttributeLookup> results = uploadService.pullAttributesFromWordPress();
        return ResponseEntity.ok(results);
    }

    // ========== UPLOAD CATEGORIES ==========

    /**
     * POST /wp-upload/categories - Upload categories to WordPress.
     * If request body is empty, uses categories from config.
     * If request body contains a list of names, uploads those.
     *
     * Body: ["Category1", "Category2"] or empty for config-based upload.
     */
    @PostMapping(value = "/categories", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<WpCategoryLookup>> uploadCategories(@RequestBody List<String> categoryNames) {
        logger.info("POST /wp-upload/categories - Uploading {} categories", categoryNames.size());
        List<WpCategoryLookup> results = uploadService.uploadCategories(categoryNames);
        return ResponseEntity.status(HttpStatus.CREATED).body(results);
    }

    @PostMapping(value = "/categories/from-config", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<WpCategoryLookup>> uploadCategoriesFromConfig() {
        logger.info("POST /wp-upload/categories/from-config - Uploading categories from config");
        List<WpCategoryLookup> results = uploadService.uploadCategoriesFromConfig();
        return ResponseEntity.status(HttpStatus.CREATED).body(results);
    }

    // ========== UPLOAD BRANDS ==========

    /**
     * POST /wp-upload/brands - Upload brands to WordPress.
     *
     * Body: ["Brand1", "Brand2"]
     */
    @PostMapping(value = "/brands", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<WpBrandLookup>> uploadBrands(@RequestBody List<String> brandNames) {
        logger.info("POST /wp-upload/brands - Uploading {} brands", brandNames.size());
        List<WpBrandLookup> results = uploadService.uploadBrands(brandNames);
        return ResponseEntity.status(HttpStatus.CREATED).body(results);
    }

    @PostMapping(value = "/brands/from-config", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<WpBrandLookup>> uploadBrandsFromConfig() {
        logger.info("POST /wp-upload/brands/from-config - Uploading brands from config");
        List<WpBrandLookup> results = uploadService.uploadBrandsFromConfig();
        return ResponseEntity.status(HttpStatus.CREATED).body(results);
    }

    // ========== UPLOAD ATTRIBUTES ==========

    /**
     * POST /wp-upload/attributes - Upload attributes to WordPress.
     *
     * Body: ["Color", "Size"]
     */
    @PostMapping(value = "/attributes", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<WpAttributeLookup>> uploadAttributes(@RequestBody List<String> attributeNames) {
        logger.info("POST /wp-upload/attributes - Uploading {} attributes", attributeNames.size());
        List<WpAttributeLookup> results = uploadService.uploadAttributes(attributeNames);
        return ResponseEntity.status(HttpStatus.CREATED).body(results);
    }

    @PostMapping(value = "/attributes/from-config", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<WpAttributeLookup>> uploadAttributesFromConfig() {
        logger.info("POST /wp-upload/attributes/from-config - Uploading attributes from config");
        List<WpAttributeLookup> results = uploadService.uploadAttributesFromConfig();
        return ResponseEntity.status(HttpStatus.CREATED).body(results);
    }

    // ========== UPLOAD MEDIA ==========

    /**
     * POST /wp-upload/media - Upload images to WordPress media library.
     * Downloads each image from the source URL and uploads it to WordPress.
     *
     * Body: ["https://example.com/image1.jpg", "https://example.com/image2.png"]
     */
    @PostMapping(value = "/media", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<WpMediaLookup>> uploadMedia(@RequestBody List<String> imageUrls) {
        logger.info("POST /wp-upload/media - Uploading {} images", imageUrls.size());
        List<WpMediaLookup> results = uploadService.uploadMedia(imageUrls);
        return ResponseEntity.status(HttpStatus.CREATED).body(results);
    }

    // ========== LOOKUP QUERIES ==========

    /**
     * GET /wp-upload/lookup - Get all lookup tables.
     */
    @GetMapping(value = "/lookup", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getAllLookups() {
        logger.info("GET /wp-upload/lookup - Fetching all lookup tables");
        Map<String, Object> lookups = Map.of(
            "categories", uploadService.getAllCategories(),
            "brands", uploadService.getAllBrands(),
            "attributes", uploadService.getAllAttributes(),
            "media", uploadService.getAllMedia()
        );
        return ResponseEntity.ok(lookups);
    }

    @GetMapping(value = "/lookup/categories", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<WpCategoryLookup>> getCategories() {
        return ResponseEntity.ok(uploadService.getAllCategories());
    }

    @GetMapping(value = "/lookup/brands", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<WpBrandLookup>> getBrands() {
        return ResponseEntity.ok(uploadService.getAllBrands());
    }

    @GetMapping(value = "/lookup/attributes", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<WpAttributeLookup>> getAttributes() {
        return ResponseEntity.ok(uploadService.getAllAttributes());
    }

    @GetMapping(value = "/lookup/media", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<WpMediaLookup>> getMedia() {
        return ResponseEntity.ok(uploadService.getAllMedia());
    }
}
