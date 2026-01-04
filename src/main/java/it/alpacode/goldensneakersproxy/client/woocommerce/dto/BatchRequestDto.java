package it.alpacode.goldensneakersproxy.client.woocommerce.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * WooCommerce Batch API request DTO.
 */
public class BatchRequestDto<T> {

    private List<T> create = new ArrayList<>();
    private List<T> update = new ArrayList<>();
    private List<DeleteItemDto> delete = new ArrayList<>();

    public BatchRequestDto() {
    }

    public static <T> BatchRequestDto<T> forCreate(List<T> items) {
        BatchRequestDto<T> batch = new BatchRequestDto<>();
        batch.setCreate(items);
        return batch;
    }

    public static <T> BatchRequestDto<T> forUpdate(List<T> items) {
        BatchRequestDto<T> batch = new BatchRequestDto<>();
        batch.setUpdate(items);
        return batch;
    }

    public static <T> BatchRequestDto<T> forDelete(List<Long> ids) {
        BatchRequestDto<T> batch = new BatchRequestDto<>();
        List<DeleteItemDto> deleteItems = new ArrayList<>();
        for (Long id : ids) {
            DeleteItemDto item = new DeleteItemDto();
            item.setId(id);
            item.setForce(true);
            deleteItems.add(item);
        }
        batch.setDelete(deleteItems);
        return batch;
    }

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

    public List<DeleteItemDto> getDelete() {
        return delete;
    }

    public void setDelete(List<DeleteItemDto> delete) {
        this.delete = delete;
    }

    public static class DeleteItemDto {
        private Long id;
        private boolean force = true;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public boolean isForce() {
            return force;
        }

        public void setForce(boolean force) {
            this.force = force;
        }
    }
}
