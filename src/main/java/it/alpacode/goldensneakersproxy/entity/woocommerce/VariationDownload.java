package it.alpacode.goldensneakersproxy.entity.woocommerce;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Table(name = "woo_variation_downloads")
public class VariationDownload {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dbId;

    @Column(name = "download_id")
    private String id;

    private String name;

    @Column(name = "file_url")
    private String file;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variation_id")
    @JsonBackReference
    private WooVariation variation;

    public VariationDownload() {
    }

    public Long getDbId() {
        return dbId;
    }

    public void setDbId(Long dbId) {
        this.dbId = dbId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public WooVariation getVariation() {
        return variation;
    }

    public void setVariation(WooVariation variation) {
        this.variation = variation;
    }
}
