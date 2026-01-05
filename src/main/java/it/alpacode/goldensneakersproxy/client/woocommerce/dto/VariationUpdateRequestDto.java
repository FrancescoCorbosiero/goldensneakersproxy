package it.alpacode.goldensneakersproxy.client.woocommerce.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * WooCommerce Variation Update Request DTO.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VariationUpdateRequestDto {

    private Long id;

    @JsonProperty("regular_price")
    private String regularPrice;

    @JsonProperty("sale_price")
    private String salePrice;

    @JsonProperty("stock_quantity")
    private Integer stockQuantity;

    @JsonProperty("stock_status")
    private String stockStatus;

    @JsonProperty("meta_data")
    private List<MetaDataDto> metaData;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public List<MetaDataDto> getMetaData() {
        return metaData;
    }

    public void setMetaData(List<MetaDataDto> metaData) {
        this.metaData = metaData;
    }
}
