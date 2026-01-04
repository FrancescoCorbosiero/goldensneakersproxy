package it.alpacode.goldensneakersproxy.model;

import it.alpacode.goldensneakersproxy.client.woocommerce.dto.AttributeDto;
import it.alpacode.goldensneakersproxy.client.woocommerce.dto.DimensionsDto;
import it.alpacode.goldensneakersproxy.client.woocommerce.dto.ImageDto;
import it.alpacode.goldensneakersproxy.client.woocommerce.dto.MetaDataDto;

import java.util.ArrayList;
import java.util.List;

/**
 * Domain model for a catalog product to be synchronized.
 */
public class CatalogProduct {

    private String sku;
    private String name;
    private String description;
    private String shortDescription;
    private String type = "variable";
    private String status = "publish";
    private String weight;
    private DimensionsDto dimensions;

    // Taxonomies
    private List<TaxonomyRef> brands = new ArrayList<>();
    private List<TaxonomyRef> tags = new ArrayList<>();
    private List<TaxonomyRef> categories = new ArrayList<>();

    // Media
    private List<ImageDto> images = new ArrayList<>();

    // Attributes (for variable products)
    private List<AttributeDto> attributes = new ArrayList<>();

    // Variations
    private List<CatalogVariation> variations = new ArrayList<>();

    // Custom metadata
    private List<MetaDataDto> metaData = new ArrayList<>();

    public CatalogProduct() {
    }

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

    public List<TaxonomyRef> getBrands() {
        return brands;
    }

    public void setBrands(List<TaxonomyRef> brands) {
        this.brands = brands;
    }

    public List<TaxonomyRef> getTags() {
        return tags;
    }

    public void setTags(List<TaxonomyRef> tags) {
        this.tags = tags;
    }

    public List<TaxonomyRef> getCategories() {
        return categories;
    }

    public void setCategories(List<TaxonomyRef> categories) {
        this.categories = categories;
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

    public List<CatalogVariation> getVariations() {
        return variations;
    }

    public void setVariations(List<CatalogVariation> variations) {
        this.variations = variations;
    }

    public List<MetaDataDto> getMetaData() {
        return metaData;
    }

    public void setMetaData(List<MetaDataDto> metaData) {
        this.metaData = metaData;
    }

    /**
     * Get all unique sizes from variations for building attributes.
     */
    public List<String> getAllSizes() {
        return variations.stream()
                .map(CatalogVariation::getSize)
                .distinct()
                .toList();
    }
}
