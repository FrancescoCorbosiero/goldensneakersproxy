package it.alpacode.goldensneakersproxy.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Result of taxonomy resolution containing mappings from TaxonomyRef to resolved IDs.
 */
public class TaxonomyResolutionResult {

    private Map<TaxonomyRef, Long> brands = new HashMap<>();
    private Map<TaxonomyRef, Long> tags = new HashMap<>();
    private Map<TaxonomyRef, Long> categories = new HashMap<>();
    private int brandsCreated;
    private int tagsCreated;
    private int categoriesCreated;

    public TaxonomyResolutionResult() {
    }

    public TaxonomyResolutionResult(Map<TaxonomyRef, Long> brands, Map<TaxonomyRef, Long> tags) {
        this.brands = brands;
        this.tags = tags;
    }

    public Map<TaxonomyRef, Long> getBrands() {
        return brands;
    }

    public void setBrands(Map<TaxonomyRef, Long> brands) {
        this.brands = brands;
    }

    public Map<TaxonomyRef, Long> getTags() {
        return tags;
    }

    public void setTags(Map<TaxonomyRef, Long> tags) {
        this.tags = tags;
    }

    public Map<TaxonomyRef, Long> getCategories() {
        return categories;
    }

    public void setCategories(Map<TaxonomyRef, Long> categories) {
        this.categories = categories;
    }

    public int getBrandsCreated() {
        return brandsCreated;
    }

    public void setBrandsCreated(int brandsCreated) {
        this.brandsCreated = brandsCreated;
    }

    public int getTagsCreated() {
        return tagsCreated;
    }

    public void setTagsCreated(int tagsCreated) {
        this.tagsCreated = tagsCreated;
    }

    public int getCategoriesCreated() {
        return categoriesCreated;
    }

    public void setCategoriesCreated(int categoriesCreated) {
        this.categoriesCreated = categoriesCreated;
    }

    /**
     * Resolve a list of brand references to their IDs.
     */
    public List<Long> resolveBrands(List<TaxonomyRef> refs) {
        if (refs == null) return List.of();
        return refs.stream()
                .map(ref -> brands.getOrDefault(ref, ref.getId()))
                .filter(id -> id != null)
                .collect(Collectors.toList());
    }

    /**
     * Resolve a list of tag references to their IDs.
     */
    public List<Long> resolveTags(List<TaxonomyRef> refs) {
        if (refs == null) return List.of();
        return refs.stream()
                .map(ref -> tags.getOrDefault(ref, ref.getId()))
                .filter(id -> id != null)
                .collect(Collectors.toList());
    }

    /**
     * Resolve a list of category references to their IDs.
     */
    public List<Long> resolveCategories(List<TaxonomyRef> refs) {
        if (refs == null) return List.of();
        return refs.stream()
                .map(ref -> categories.getOrDefault(ref, ref.getId()))
                .filter(id -> id != null)
                .collect(Collectors.toList());
    }

    /**
     * Get total count of created taxonomies.
     */
    public int getCreatedCount() {
        return brandsCreated + tagsCreated + categoriesCreated;
    }
}
