package it.alpacode.goldensneakersproxy.client.woocommerce.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * WooCommerce Product Category DTO.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CategoryDto {

    private Long id;
    private String name;
    private String slug;

    public CategoryDto() {
    }

    public CategoryDto(Long id) {
        this.id = id;
    }

    public CategoryDto(Long id, String name, String slug) {
        this.id = id;
        this.name = name;
        this.slug = slug;
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

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }
}
