package it.alpacode.goldensneakersproxy.entity.woocommerce;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Table(name = "woo_variation_attributes")
public class VariationAttribute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dbId;

    @Column(name = "attribute_id")
    private Integer id;

    private String name;

    @Column(name = "option_value")
    private String option;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variation_id")
    @JsonBackReference
    private WooVariation variation;

    public VariationAttribute() {
    }

    public Long getDbId() {
        return dbId;
    }

    public void setDbId(Long dbId) {
        this.dbId = dbId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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

    public WooVariation getVariation() {
        return variation;
    }

    public void setVariation(WooVariation variation) {
        this.variation = variation;
    }
}
