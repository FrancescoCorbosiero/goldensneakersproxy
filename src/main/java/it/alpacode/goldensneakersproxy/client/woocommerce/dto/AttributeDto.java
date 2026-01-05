package it.alpacode.goldensneakersproxy.client.woocommerce.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * WooCommerce Product Attribute DTO.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AttributeDto {

    private Long id;
    private String name;
    private Integer position;
    private Boolean visible;
    private Boolean variation;
    private List<String> options = new ArrayList<>();

    public AttributeDto() {
    }

    public AttributeDto(String name, List<String> options) {
        this.name = name;
        this.options = options;
        this.visible = true;
        this.variation = true;
    }

    public AttributeDto(Long id, String name, List<String> options) {
        this.id = id;
        this.name = name;
        this.options = options;
        this.visible = true;
        this.variation = true;
    }

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

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public Boolean getVariation() {
        return variation;
    }

    public void setVariation(Boolean variation) {
        this.variation = variation;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }
}
