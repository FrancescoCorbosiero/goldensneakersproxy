package it.alpacode.goldensneakersproxy.client.woocommerce.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * WooCommerce Product Update Request DTO.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductUpdateRequestDto {

    private Long id;
    private String name;
    private String description;

    @JsonProperty("short_description")
    private String shortDescription;

    @JsonProperty("stock_status")
    private String stockStatus;

    private List<TagDto> tags;
    private List<ImageDto> images;

    @JsonProperty("meta_data")
    private List<MetaDataDto> metaData;

    @JsonProperty("brands")
    private List<Long> brands;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getStockStatus() {
        return stockStatus;
    }

    public void setStockStatus(String stockStatus) {
        this.stockStatus = stockStatus;
    }

    public List<TagDto> getTags() {
        return tags;
    }

    public void setTags(List<TagDto> tags) {
        this.tags = tags;
    }

    public List<ImageDto> getImages() {
        return images;
    }

    public void setImages(List<ImageDto> images) {
        this.images = images;
    }

    public List<MetaDataDto> getMetaData() {
        return metaData;
    }

    public void setMetaData(List<MetaDataDto> metaData) {
        this.metaData = metaData;
    }

    public List<Long> getBrands() {
        return brands;
    }

    public void setBrands(List<Long> brands) {
        this.brands = brands;
    }
}
