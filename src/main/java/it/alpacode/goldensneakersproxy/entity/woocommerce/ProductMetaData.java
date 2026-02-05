package it.alpacode.goldensneakersproxy.entity.woocommerce;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Table(name = "woo_product_meta_data")
public class ProductMetaData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dbId;

    @Column(name = "meta_id")
    private Integer id;

    @Column(name = "meta_key")
    private String key;

    @Column(name = "meta_value", columnDefinition = "TEXT")
    private String value;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @JsonBackReference
    private WooProduct product;

    public ProductMetaData() {
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

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public WooProduct getProduct() {
        return product;
    }

    public void setProduct(WooProduct product) {
        this.product = product;
    }
}
