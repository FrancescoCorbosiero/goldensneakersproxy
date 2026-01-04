package it.alpacode.goldensneakersproxy.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the difference between feed products and shop products.
 */
public class CatalogDiff {

    private List<CatalogProduct> toCreate = new ArrayList<>();
    private List<CatalogProduct> toUpdate = new ArrayList<>();
    private List<Long> toMarkOutOfStock = new ArrayList<>();

    public CatalogDiff() {
    }

    public CatalogDiff(List<CatalogProduct> toCreate, List<CatalogProduct> toUpdate, List<Long> toMarkOutOfStock) {
        this.toCreate = toCreate;
        this.toUpdate = toUpdate;
        this.toMarkOutOfStock = toMarkOutOfStock;
    }

    public List<CatalogProduct> getToCreate() {
        return toCreate;
    }

    public void setToCreate(List<CatalogProduct> toCreate) {
        this.toCreate = toCreate;
    }

    public List<CatalogProduct> getToUpdate() {
        return toUpdate;
    }

    public void setToUpdate(List<CatalogProduct> toUpdate) {
        this.toUpdate = toUpdate;
    }

    public List<Long> getToMarkOutOfStock() {
        return toMarkOutOfStock;
    }

    public void setToMarkOutOfStock(List<Long> toMarkOutOfStock) {
        this.toMarkOutOfStock = toMarkOutOfStock;
    }

    public boolean isEmpty() {
        return toCreate.isEmpty() && toUpdate.isEmpty() && toMarkOutOfStock.isEmpty();
    }

    public int getTotalChanges() {
        return toCreate.size() + toUpdate.size() + toMarkOutOfStock.size();
    }
}
