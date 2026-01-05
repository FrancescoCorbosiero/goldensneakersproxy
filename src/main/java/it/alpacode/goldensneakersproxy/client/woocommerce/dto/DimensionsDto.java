package it.alpacode.goldensneakersproxy.client.woocommerce.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * WooCommerce Product Dimensions DTO.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DimensionsDto {

    private String length;
    private String width;
    private String height;

    public DimensionsDto() {
    }

    public DimensionsDto(String length, String width, String height) {
        this.length = length;
        this.width = width;
        this.height = height;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }
}
