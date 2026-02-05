package it.alpacode.goldensneakersproxy.entity.woocommerce;

import jakarta.persistence.Embeddable;

@Embeddable
public class ProductDimensions {

    private String length;
    private String width;
    private String height;

    public ProductDimensions() {
    }

    public ProductDimensions(String length, String width, String height) {
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
