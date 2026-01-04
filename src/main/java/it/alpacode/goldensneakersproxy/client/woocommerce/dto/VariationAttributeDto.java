package it.alpacode.goldensneakersproxy.client.woocommerce.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * WooCommerce Variation Attribute DTO.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class VariationAttributeDto {

    private Long id;
    private String name;
    private String option;

    public VariationAttributeDto() {
    }

    public VariationAttributeDto(String name, String option) {
        this.name = name;
        this.option = option;
    }

    public VariationAttributeDto(Long id, String name, String option) {
        this.id = id;
        this.name = name;
        this.option = option;
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

    public String getOption() {
        return option;
    }

    public void setOption(String option) {
        this.option = option;
    }
}
