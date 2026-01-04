package it.alpacode.goldensneakersproxy.client.wordpress.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * WordPress/WooCommerce Brand taxonomy DTO.
 * Compatible with WooCommerce Brands plugin and Perfect Brands for WooCommerce.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BrandDto {

    private Long id;
    private String name;
    private String slug;
    private String description;
    private Long parent;
    private Integer count;

    public BrandDto() {
    }

    public BrandDto(Long id) {
        this.id = id;
    }

    public BrandDto(String name, String slug) {
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getParent() {
        return parent;
    }

    public void setParent(Long parent) {
        this.parent = parent;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
