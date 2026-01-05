package it.alpacode.goldensneakersproxy.model;

import it.alpacode.goldensneakersproxy.client.woocommerce.dto.DimensionsDto;
import it.alpacode.goldensneakersproxy.client.woocommerce.dto.MetaDataDto;

import java.util.ArrayList;
import java.util.List;

/**
 * Domain model for a product variation (size/color variant).
 */
public class CatalogVariation {

    private String sku;
    private String size;
    private String regularPrice;
    private String salePrice;
    private Integer stockQuantity;
    private String stockStatus;
    private String weight;
    private DimensionsDto dimensions;
    private String imageUrl;
    private List<MetaDataDto> metaData = new ArrayList<>();

    public CatalogVariation() {
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getRegularPrice() {
        return regularPrice;
    }

    public void setRegularPrice(String regularPrice) {
        this.regularPrice = regularPrice;
    }

    public String getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(String salePrice) {
        this.salePrice = salePrice;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public String getStockStatus() {
        return stockStatus;
    }

    public void setStockStatus(String stockStatus) {
        this.stockStatus = stockStatus;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<MetaDataDto> getMetaData() {
        return metaData;
    }

    public void setMetaData(List<MetaDataDto> metaData) {
        this.metaData = metaData;
    }
}
