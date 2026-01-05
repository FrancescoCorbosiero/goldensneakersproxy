package it.alpacode.goldensneakersproxy.controller.dto;

import it.alpacode.goldensneakersproxy.client.woocommerce.dto.AttributeDto;
import it.alpacode.goldensneakersproxy.client.woocommerce.dto.DimensionsDto;
import it.alpacode.goldensneakersproxy.client.woocommerce.dto.ImageDto;
import it.alpacode.goldensneakersproxy.client.woocommerce.dto.MetaDataDto;
import it.alpacode.goldensneakersproxy.model.CatalogProduct;
import it.alpacode.goldensneakersproxy.model.CatalogVariation;
import it.alpacode.goldensneakersproxy.model.TaxonomyRef;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Request DTO for catalog product sync.
 */
public class CatalogProductRequest {

    private String sku;
    private String name;
    private String description;
    private String shortDescription;
    private String type = "variable";
    private String status = "publish";
    private String weight;
    private DimensionsDto dimensions;

    // Taxonomies - can be IDs or names
    private List<String> brands = new ArrayList<>();
    private List<String> tags = new ArrayList<>();
    private List<Long> categoryIds = new ArrayList<>();

    // Media
    private List<ImageDto> images = new ArrayList<>();

    // Attributes
    private List<AttributeDto> attributes = new ArrayList<>();

    // Variations
    private List<CatalogVariationRequest> variations = new ArrayList<>();

    // Custom metadata
    private List<MetaDataDto> metaData = new ArrayList<>();

    public CatalogProductRequest() {
    }

    /**
     * Convert to domain model.
     */
    public CatalogProduct toDomain() {
        CatalogProduct product = new CatalogProduct();
        product.setSku(sku);
        product.setName(name);
        product.setDescription(description);
        product.setShortDescription(shortDescription);
        product.setType(type);
        product.setStatus(status);
        product.setWeight(weight);
        product.setDimensions(dimensions);
        product.setImages(images);
        product.setAttributes(attributes);
        product.setMetaData(metaData);

        // Convert brand names to TaxonomyRefs
        product.setBrands(brands.stream()
                .map(TaxonomyRef::ofName)
                .collect(Collectors.toList()));

        // Convert tag names to TaxonomyRefs
        product.setTags(tags.stream()
                .map(TaxonomyRef::ofName)
                .collect(Collectors.toList()));

        // Convert category IDs to TaxonomyRefs
        product.setCategories(categoryIds.stream()
                .map(TaxonomyRef::ofId)
                .collect(Collectors.toList()));

        // Convert variations
        product.setVariations(variations.stream()
                .map(CatalogVariationRequest::toDomain)
                .collect(Collectors.toList()));

        return product;
    }

    // Getters and setters

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public DimensionsDto getDimensions() {
        return dimensions;
    }

    public void setDimensions(DimensionsDto dimensions) {
        this.dimensions = dimensions;
    }

    public List<String> getBrands() {
        return brands;
    }

    public void setBrands(List<String> brands) {
        this.brands = brands;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<Long> getCategoryIds() {
        return categoryIds;
    }

    public void setCategoryIds(List<Long> categoryIds) {
        this.categoryIds = categoryIds;
    }

    public List<ImageDto> getImages() {
        return images;
    }

    public void setImages(List<ImageDto> images) {
        this.images = images;
    }

    public List<AttributeDto> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<AttributeDto> attributes) {
        this.attributes = attributes;
    }

    public List<CatalogVariationRequest> getVariations() {
        return variations;
    }

    public void setVariations(List<CatalogVariationRequest> variations) {
        this.variations = variations;
    }

    public List<MetaDataDto> getMetaData() {
        return metaData;
    }

    public void setMetaData(List<MetaDataDto> metaData) {
        this.metaData = metaData;
    }
}
