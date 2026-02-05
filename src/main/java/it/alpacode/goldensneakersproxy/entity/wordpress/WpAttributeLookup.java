package it.alpacode.goldensneakersproxy.entity.wordpress;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

@Entity
@Table(name = "wp_attribute_lookup")
public class WpAttributeLookup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String slug;

    @Column(name = "wordpress_id", nullable = false)
    @JsonProperty("wordpress_id")
    private Integer wordpressId;

    public WpAttributeLookup() {
    }

    public WpAttributeLookup(String name, String slug, Integer wordpressId) {
        this.name = name;
        this.slug = slug;
        this.wordpressId = wordpressId;
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

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public Integer getWordpressId() {
        return wordpressId;
    }

    public void setWordpressId(Integer wordpressId) {
        this.wordpressId = wordpressId;
    }
}
