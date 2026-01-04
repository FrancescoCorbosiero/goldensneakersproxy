package it.alpacode.goldensneakersproxy.model;

import java.util.Objects;

/**
 * Reference to a taxonomy (brand, tag, category) that can be either an ID or a name.
 * Allows flexible input where taxonomies can be specified by ID (if known) or name (to be resolved).
 */
public class TaxonomyRef {

    private Long id;
    private String name;

    public TaxonomyRef() {
    }

    private TaxonomyRef(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Create a reference by ID.
     */
    public static TaxonomyRef ofId(Long id) {
        return new TaxonomyRef(id, null);
    }

    /**
     * Create a reference by name (to be resolved).
     */
    public static TaxonomyRef ofName(String name) {
        return new TaxonomyRef(null, name);
    }

    /**
     * Check if this is an ID reference.
     */
    public boolean isId() {
        return id != null;
    }

    /**
     * Check if this is a name reference.
     */
    public boolean isName() {
        return name != null && id == null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the key for caching/comparison - uses name if available, otherwise ID.
     */
    public String getKey() {
        if (name != null) {
            return slugify(name);
        }
        return id != null ? id.toString() : "";
    }

    private String slugify(String text) {
        if (text == null) {
            return "";
        }
        return text.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaxonomyRef that = (TaxonomyRef) o;
        return Objects.equals(getKey(), that.getKey());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getKey());
    }

    @Override
    public String toString() {
        if (id != null) {
            return "TaxonomyRef{id=" + id + "}";
        }
        return "TaxonomyRef{name='" + name + "'}";
    }
}
