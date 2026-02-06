package it.alpacode.goldensneakersproxy.entity.wordpress;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

@Entity
@Table(name = "wp_media_lookup")
public class WpMediaLookup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_url", length = 2048, nullable = false, unique = true)
    @JsonProperty("source_url")
    private String sourceUrl;

    @Column(name = "wordpress_id", nullable = false)
    @JsonProperty("wordpress_id")
    private Integer wordpressId;

    @Column(name = "uploaded_url", length = 2048)
    @JsonProperty("uploaded_url")
    private String uploadedUrl;

    public WpMediaLookup() {
    }

    public WpMediaLookup(String sourceUrl, Integer wordpressId, String uploadedUrl) {
        this.sourceUrl = sourceUrl;
        this.wordpressId = wordpressId;
        this.uploadedUrl = uploadedUrl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public Integer getWordpressId() {
        return wordpressId;
    }

    public void setWordpressId(Integer wordpressId) {
        this.wordpressId = wordpressId;
    }

    public String getUploadedUrl() {
        return uploadedUrl;
    }

    public void setUploadedUrl(String uploadedUrl) {
        this.uploadedUrl = uploadedUrl;
    }
}
