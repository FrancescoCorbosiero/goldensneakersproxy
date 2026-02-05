package it.alpacode.goldensneakersproxy.entity.woocommerce;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "woo_product_images")
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_created")
    @JsonProperty("date_created")
    private OffsetDateTime dateCreated;

    @Column(name = "date_created_gmt")
    @JsonProperty("date_created_gmt")
    private OffsetDateTime dateCreatedGmt;

    @Column(name = "date_modified")
    @JsonProperty("date_modified")
    private OffsetDateTime dateModified;

    @Column(name = "date_modified_gmt")
    @JsonProperty("date_modified_gmt")
    private OffsetDateTime dateModifiedGmt;

    @Column(length = 2048)
    private String src;

    private String name;

    @Column(length = 1024)
    private String alt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @JsonBackReference
    private WooProduct product;

    public ProductImage() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public OffsetDateTime getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(OffsetDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }

    public OffsetDateTime getDateCreatedGmt() {
        return dateCreatedGmt;
    }

    public void setDateCreatedGmt(OffsetDateTime dateCreatedGmt) {
        this.dateCreatedGmt = dateCreatedGmt;
    }

    public OffsetDateTime getDateModified() {
        return dateModified;
    }

    public void setDateModified(OffsetDateTime dateModified) {
        this.dateModified = dateModified;
    }

    public OffsetDateTime getDateModifiedGmt() {
        return dateModifiedGmt;
    }

    public void setDateModifiedGmt(OffsetDateTime dateModifiedGmt) {
        this.dateModifiedGmt = dateModifiedGmt;
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

    public WooProduct getProduct() {
        return product;
    }

    public void setProduct(WooProduct product) {
        this.product = product;
    }
}
