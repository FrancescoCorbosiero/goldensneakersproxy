package it.alpacode.goldensneakersproxy.client.wordpress.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * WordPress Taxonomy Create Request DTO.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaxonomyCreateRequestDto {

    private String name;
    private String slug;
    private String description;
    private Long parent;

    public TaxonomyCreateRequestDto() {
    }

    public TaxonomyCreateRequestDto(String name, String slug) {
        this.name = name;
        this.slug = slug;
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
}
