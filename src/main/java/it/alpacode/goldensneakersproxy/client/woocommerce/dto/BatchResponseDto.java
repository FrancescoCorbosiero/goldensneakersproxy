package it.alpacode.goldensneakersproxy.client.woocommerce.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * WooCommerce Batch API response DTO.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BatchResponseDto<T> {

    private List<T> create = new ArrayList<>();
    private List<T> update = new ArrayList<>();
    private List<T> delete = new ArrayList<>();

    public List<T> getCreate() {
        return create;
    }

    public void setCreate(List<T> create) {
        this.create = create;
    }

    public List<T> getUpdate() {
        return update;
    }

    public void setUpdate(List<T> update) {
        this.update = update;
    }

    public List<T> getDelete() {
        return delete;
    }

    public void setDelete(List<T> delete) {
        this.delete = delete;
    }
}
