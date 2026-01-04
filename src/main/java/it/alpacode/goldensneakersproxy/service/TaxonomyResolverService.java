package it.alpacode.goldensneakersproxy.service;

import it.alpacode.goldensneakersproxy.client.wordpress.WordPressClient;
import it.alpacode.goldensneakersproxy.client.wordpress.dto.BrandDto;
import it.alpacode.goldensneakersproxy.client.wordpress.dto.TaxonomyCreateRequestDto;
import it.alpacode.goldensneakersproxy.client.wordpress.dto.WpTagDto;
import it.alpacode.goldensneakersproxy.model.CatalogProduct;
import it.alpacode.goldensneakersproxy.model.TaxonomyRef;
import it.alpacode.goldensneakersproxy.model.TaxonomyResolutionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service for resolving taxonomies (brands, tags) in bulk.
 * Caches existing taxonomies and creates missing ones.
 */
@Service
public class TaxonomyResolverService {

    private static final Logger log = LoggerFactory.getLogger(TaxonomyResolverService.class);

    private final WordPressClient wpClient;

    // Caches for taxonomy slug -> ID mapping
    private final Map<String, Long> brandCache = new ConcurrentHashMap<>();
    private final Map<String, Long> tagCache = new ConcurrentHashMap<>();

    public TaxonomyResolverService(WordPressClient wpClient) {
        this.wpClient = wpClient;
    }

    /**
     * Resolve all taxonomies from a list of products in bulk.
     * 1. Pre-fetch existing taxonomies (warm cache)
     * 2. Identify missing taxonomies
     * 3. Create missing taxonomies
     * 4. Return complete mapping
     */
    public TaxonomyResolutionResult resolveAll(List<CatalogProduct> products) {
        log.info("Starting taxonomy resolution for {} products", products.size());

        // Warm up cache
        warmCache();

        // Collect unique taxonomies from all products
        Set<TaxonomyRef> allBrands = products.stream()
                .flatMap(p -> p.getBrands().stream())
                .collect(Collectors.toSet());

        Set<TaxonomyRef> allTags = products.stream()
                .flatMap(p -> p.getTags().stream())
                .collect(Collectors.toSet());

        log.info("Found {} unique brands and {} unique tags to resolve",
                allBrands.size(), allTags.size());

        // Resolve brands
        Map<TaxonomyRef, Long> brandMap = resolveBrandsBulk(allBrands);

        // Resolve tags
        Map<TaxonomyRef, Long> tagMap = resolveTagsBulk(allTags);

        TaxonomyResolutionResult result = new TaxonomyResolutionResult(brandMap, tagMap);

        log.info("Taxonomy resolution complete - {} brands, {} tags resolved",
                brandMap.size(), tagMap.size());

        return result;
    }

    /**
     * Warm up the taxonomy caches by fetching all existing taxonomies.
     */
    public void warmCache() {
        log.info("Warming taxonomy cache...");

        // Clear existing cache
        brandCache.clear();
        tagCache.clear();

        // Fetch brands
        try {
            List<BrandDto> brands = wpClient.listAllBrands(100);
            for (BrandDto brand : brands) {
                if (brand.getSlug() != null) {
                    brandCache.put(brand.getSlug(), brand.getId());
                }
            }
            log.debug("Cached {} brands", brandCache.size());
        } catch (Exception e) {
            log.warn("Failed to warm brand cache: {}", e.getMessage());
        }

        // Fetch tags
        try {
            List<WpTagDto> tags = wpClient.listAllTags(100);
            for (WpTagDto tag : tags) {
                if (tag.getSlug() != null) {
                    tagCache.put(tag.getSlug(), tag.getId());
                }
            }
            log.debug("Cached {} tags", tagCache.size());
        } catch (Exception e) {
            log.warn("Failed to warm tag cache: {}", e.getMessage());
        }

        log.info("Cache warmed: {} brands, {} tags", brandCache.size(), tagCache.size());
    }

    /**
     * Resolve brands in bulk - check cache and create missing ones.
     */
    private Map<TaxonomyRef, Long> resolveBrandsBulk(Set<TaxonomyRef> refs) {
        Map<TaxonomyRef, Long> resolved = new HashMap<>();
        List<TaxonomyRef> toCreate = new ArrayList<>();

        // Check cache for each reference
        for (TaxonomyRef ref : refs) {
            if (ref.isId()) {
                // Already have ID
                resolved.put(ref, ref.getId());
            } else if (ref.getName() != null) {
                String slug = slugify(ref.getName());
                if (brandCache.containsKey(slug)) {
                    resolved.put(ref, brandCache.get(slug));
                } else {
                    toCreate.add(ref);
                }
            }
        }

        if (toCreate.isEmpty()) {
            return resolved;
        }

        log.info("Creating {} new brands", toCreate.size());

        // Create missing brands
        List<TaxonomyCreateRequestDto> requests = toCreate.stream()
                .map(ref -> new TaxonomyCreateRequestDto(ref.getName(), slugify(ref.getName())))
                .toList();

        List<BrandDto> created = wpClient.createBrandsBatch(requests);

        // Map created brands back to refs
        Map<String, BrandDto> createdBySlug = created.stream()
                .filter(b -> b.getSlug() != null)
                .collect(Collectors.toMap(BrandDto::getSlug, b -> b, (a, b) -> a));

        for (TaxonomyRef ref : toCreate) {
            String slug = slugify(ref.getName());
            BrandDto brand = createdBySlug.get(slug);
            if (brand != null) {
                resolved.put(ref, brand.getId());
                brandCache.put(slug, brand.getId());
            }
        }

        return resolved;
    }

    /**
     * Resolve tags in bulk - check cache and create missing ones.
     */
    private Map<TaxonomyRef, Long> resolveTagsBulk(Set<TaxonomyRef> refs) {
        Map<TaxonomyRef, Long> resolved = new HashMap<>();
        List<TaxonomyRef> toCreate = new ArrayList<>();

        // Check cache for each reference
        for (TaxonomyRef ref : refs) {
            if (ref.isId()) {
                // Already have ID
                resolved.put(ref, ref.getId());
            } else if (ref.getName() != null) {
                String slug = slugify(ref.getName());
                if (tagCache.containsKey(slug)) {
                    resolved.put(ref, tagCache.get(slug));
                } else {
                    toCreate.add(ref);
                }
            }
        }

        if (toCreate.isEmpty()) {
            return resolved;
        }

        log.info("Creating {} new tags", toCreate.size());

        // Create missing tags
        List<TaxonomyCreateRequestDto> requests = toCreate.stream()
                .map(ref -> new TaxonomyCreateRequestDto(ref.getName(), slugify(ref.getName())))
                .toList();

        List<WpTagDto> created = wpClient.createTagsBatch(requests);

        // Map created tags back to refs
        Map<String, WpTagDto> createdBySlug = created.stream()
                .filter(t -> t.getSlug() != null)
                .collect(Collectors.toMap(WpTagDto::getSlug, t -> t, (a, b) -> a));

        for (TaxonomyRef ref : toCreate) {
            String slug = slugify(ref.getName());
            WpTagDto tag = createdBySlug.get(slug);
            if (tag != null) {
                resolved.put(ref, tag.getId());
                tagCache.put(slug, tag.getId());
            }
        }

        return resolved;
    }

    /**
     * Convert text to slug format.
     */
    private String slugify(String text) {
        if (text == null) {
            return "";
        }
        return text.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
    }

    /**
     * Clear the taxonomy caches.
     */
    public void clearCache() {
        brandCache.clear();
        tagCache.clear();
        log.info("Taxonomy cache cleared");
    }
}
