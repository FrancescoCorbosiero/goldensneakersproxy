package it.alpacode.goldensneakersproxy.client.woocommerce.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * WooCommerce Product Image DTO.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ImageDto {

    private Long id;
    private String src;
    private String name;
    private String alt;
    private Integer position;

    public ImageDto() {
    }

    public ImageDto(String src) {
        this.src = src;
    }

    public ImageDto(String src, String name, String alt) {
        this.src = src;
        this.name = name;
        this.alt = alt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlt() {
        return alt;
    }

    public void setAlt(String alt) {
        this.alt = alt;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }
}
