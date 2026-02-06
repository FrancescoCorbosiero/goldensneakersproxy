package it.alpacode.goldensneakersproxy.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "assortment-mapper")
public class AssortmentMapperConfig {

    private String defaultCategory = "Sneakers";
    private String sizeAttributeName = "Size";
    private double markupPercentage = 47;

    public String getDefaultCategory() {
        return defaultCategory;
    }

    public void setDefaultCategory(String defaultCategory) {
        this.defaultCategory = defaultCategory;
    }

    public String getSizeAttributeName() {
        return sizeAttributeName;
    }

    public void setSizeAttributeName(String sizeAttributeName) {
        this.sizeAttributeName = sizeAttributeName;
    }

    public double getMarkupPercentage() {
        return markupPercentage;
    }

    public void setMarkupPercentage(double markupPercentage) {
        this.markupPercentage = markupPercentage;
    }
}
