package it.alpacode.goldensneakersproxy.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GsProduct {

    private Integer id;
    private String sku;
    private String name;

    @JsonProperty("brand_name")
    private String brandName;

    private String image;

    @JsonProperty("image_full_url")
    private String imageFullUrl;

    @JsonProperty("size_mapper_name")
    private String sizeMapperName;

    @JsonProperty("available_summary_quantity")
    private Integer availableSummaryQuantity;

    private List<GsSize> sizes = new ArrayList<>();

    public GsProduct() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getImageFullUrl() {
        return imageFullUrl;
    }

    public void setImageFullUrl(String imageFullUrl) {
        this.imageFullUrl = imageFullUrl;
    }

    public String getSizeMapperName() {
        return sizeMapperName;
    }

    public void setSizeMapperName(String sizeMapperName) {
        this.sizeMapperName = sizeMapperName;
    }

    public Integer getAvailableSummaryQuantity() {
        return availableSummaryQuantity;
    }

    public void setAvailableSummaryQuantity(Integer availableSummaryQuantity) {
        this.availableSummaryQuantity = availableSummaryQuantity;
    }

    public List<GsSize> getSizes() {
        return sizes;
    }

    public void setSizes(List<GsSize> sizes) {
        this.sizes = sizes;
    }
}
