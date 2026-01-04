package it.alpacode.goldensneakersproxy.client.wordpress.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * WordPress/WooCommerce Product Tag DTO.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WpTagDto {

    private Long id;
    private String name;
    private String slug;
    private String description;
    private Integer count;

    public WpTagDto() {
    }

    public WpTagDto(Long id) {
        this.id = id;
    }

    public WpTagDto(String name, String slug) {
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

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
