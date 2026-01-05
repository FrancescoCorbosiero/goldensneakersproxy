package it.alpacode.goldensneakersproxy.client.woocommerce.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * WooCommerce Meta Data DTO.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MetaDataDto {

    private Long id;
    private String key;
    private Object value;

    public MetaDataDto() {
    }

    public MetaDataDto(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
