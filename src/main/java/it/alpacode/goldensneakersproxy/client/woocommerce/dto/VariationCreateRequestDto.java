package it.alpacode.goldensneakersproxy.client.woocommerce.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * WooCommerce Variation Create Request DTO.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VariationCreateRequestDto {

    private String sku;

    @JsonProperty("regular_price")
    private String regularPrice;

    @JsonProperty("sale_price")
    private String salePrice;

    @JsonProperty("stock_quantity")
    private Integer stockQuantity;

    @JsonProperty("manage_stock")
    private Boolean manageStock = true;

    @JsonProperty("stock_status")
    private String stockStatus;

    private String weight;
    private DimensionsDto dimensions;
    private ImageDto image;

    private List<VariationAttributeDto> attributes = new ArrayList<>();

    @JsonProperty("meta_data")
    private List<MetaDataDto> metaData = new ArrayList<>();

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
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

    public Boolean getManageStock() {
        return manageStock;
    }

    public void setManageStock(Boolean manageStock) {
        this.manageStock = manageStock;
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

    public ImageDto getImage() {
        return image;
    }

    public void setImage(ImageDto image) {
        this.image = image;
    }

    public List<VariationAttributeDto> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<VariationAttributeDto> attributes) {
        this.attributes = attributes;
    }

    public List<MetaDataDto> getMetaData() {
        return metaData;
    }

    public void setMetaData(List<MetaDataDto> metaData) {
        this.metaData = metaData;
    }
}
