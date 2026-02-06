package it.alpacode.goldensneakersproxy.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GsSize {

    private Integer id;
    private String barcode;

    @JsonProperty("size_us")
    private String sizeUs;

    @JsonProperty("size_eu")
    private String sizeEu;

    @JsonProperty("offer_price")
    private BigDecimal offerPrice;

    @JsonProperty("presented_price")
    private BigDecimal presentedPrice;

    @JsonProperty("available_quantity")
    private Integer availableQuantity;

    public GsSize() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getSizeUs() {
        return sizeUs;
    }

    public void setSizeUs(String sizeUs) {
        this.sizeUs = sizeUs;
    }

    public String getSizeEu() {
        return sizeEu;
    }

    public void setSizeEu(String sizeEu) {
        this.sizeEu = sizeEu;
    }

    public BigDecimal getOfferPrice() {
        return offerPrice;
    }

    public void setOfferPrice(BigDecimal offerPrice) {
        this.offerPrice = offerPrice;
    }

    public BigDecimal getPresentedPrice() {
        return presentedPrice;
    }

    public void setPresentedPrice(BigDecimal presentedPrice) {
        this.presentedPrice = presentedPrice;
    }

    public Integer getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(Integer availableQuantity) {
        this.availableQuantity = availableQuantity;
    }
}
