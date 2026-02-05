package it.alpacode.goldensneakersproxy.entity.woocommerce;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class VariationImage {

    @Column(name = "image_id")
    private Integer id;

    @Column(name = "image_src", length = 2048)
    private String src;

    @Column(name = "image_name")
    private String name;

    @Column(name = "image_alt", length = 1024)
    private String alt;

    public VariationImage() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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
}
