package it.alpacode.goldensneakersproxy.entity.woocommerce;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "woo_products")
public class WooProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String slug;

    private String permalink;

    @Column(name = "date_created")
    @JsonProperty("date_created")
    private OffsetDateTime dateCreated;

    @Column(name = "date_created_gmt")
    @JsonProperty("date_created_gmt")
    private OffsetDateTime dateCreatedGmt;

    @Column(name = "date_modified")
    @JsonProperty("date_modified")
    private OffsetDateTime dateModified;

    @Column(name = "date_modified_gmt")
    @JsonProperty("date_modified_gmt")
    private OffsetDateTime dateModifiedGmt;

    private String type;

    private String status;

    private Boolean featured;

    @Column(name = "catalog_visibility")
    @JsonProperty("catalog_visibility")
    private String catalogVisibility;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "short_description", columnDefinition = "TEXT")
    @JsonProperty("short_description")
    private String shortDescription;

    private String sku;

    @Column(name = "global_unique_id")
    @JsonProperty("global_unique_id")
    private String globalUniqueId;

    private String price;

    @Column(name = "regular_price")
    @JsonProperty("regular_price")
    private String regularPrice;

    @Column(name = "sale_price")
    @JsonProperty("sale_price")
    private String salePrice;

    @Column(name = "date_on_sale_from")
    @JsonProperty("date_on_sale_from")
    private OffsetDateTime dateOnSaleFrom;

    @Column(name = "date_on_sale_from_gmt")
    @JsonProperty("date_on_sale_from_gmt")
    private OffsetDateTime dateOnSaleFromGmt;

    @Column(name = "date_on_sale_to")
    @JsonProperty("date_on_sale_to")
    private OffsetDateTime dateOnSaleTo;

    @Column(name = "date_on_sale_to_gmt")
    @JsonProperty("date_on_sale_to_gmt")
    private OffsetDateTime dateOnSaleToGmt;

    @Column(name = "price_html", columnDefinition = "TEXT")
    @JsonProperty("price_html")
    private String priceHtml;

    @Column(name = "on_sale")
    @JsonProperty("on_sale")
    private Boolean onSale;

    private Boolean purchasable;

    @Column(name = "total_sales")
    @JsonProperty("total_sales")
    private Integer totalSales;

    private Boolean virtual;

    private Boolean downloadable;

    @Column(name = "download_limit")
    @JsonProperty("download_limit")
    private Integer downloadLimit;

    @Column(name = "download_expiry")
    @JsonProperty("download_expiry")
    private Integer downloadExpiry;

    @Column(name = "external_url", length = 2048)
    @JsonProperty("external_url")
    private String externalUrl;

    @Column(name = "button_text")
    @JsonProperty("button_text")
    private String buttonText;

    @Column(name = "tax_status")
    @JsonProperty("tax_status")
    private String taxStatus;

    @Column(name = "tax_class")
    @JsonProperty("tax_class")
    private String taxClass;

    @Column(name = "manage_stock")
    @JsonProperty("manage_stock")
    private Boolean manageStock;

    @Column(name = "stock_quantity")
    @JsonProperty("stock_quantity")
    private Integer stockQuantity;

    @Column(name = "stock_status")
    @JsonProperty("stock_status")
    private String stockStatus;

    private String backorders;

    @Column(name = "backorders_allowed")
    @JsonProperty("backorders_allowed")
    private Boolean backordersAllowed;

    private Boolean backordered;

    @Column(name = "low_stock_amount")
    @JsonProperty("low_stock_amount")
    private Integer lowStockAmount;

    @Column(name = "sold_individually")
    @JsonProperty("sold_individually")
    private Boolean soldIndividually;

    private String weight;

    @Embedded
    private ProductDimensions dimensions;

    @Column(name = "shipping_required")
    @JsonProperty("shipping_required")
    private Boolean shippingRequired;

    @Column(name = "shipping_taxable")
    @JsonProperty("shipping_taxable")
    private Boolean shippingTaxable;

    @Column(name = "shipping_class")
    @JsonProperty("shipping_class")
    private String shippingClass;

    @Column(name = "shipping_class_id")
    @JsonProperty("shipping_class_id")
    private Integer shippingClassId;

    @Column(name = "reviews_allowed")
    @JsonProperty("reviews_allowed")
    private Boolean reviewsAllowed;

    @Column(name = "average_rating")
    @JsonProperty("average_rating")
    private String averageRating;

    @Column(name = "rating_count")
    @JsonProperty("rating_count")
    private Integer ratingCount;

    @ElementCollection
    @CollectionTable(name = "woo_product_related_ids", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "related_id")
    @JsonProperty("related_ids")
    private List<Integer> relatedIds = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "woo_product_upsell_ids", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "upsell_id")
    @JsonProperty("upsell_ids")
    private List<Integer> upsellIds = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "woo_product_cross_sell_ids", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "cross_sell_id")
    @JsonProperty("cross_sell_ids")
    private List<Integer> crossSellIds = new ArrayList<>();

    @Column(name = "parent_id")
    @JsonProperty("parent_id")
    private Integer parentId;

    @Column(name = "purchase_note", columnDefinition = "TEXT")
    @JsonProperty("purchase_note")
    private String purchaseNote;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ProductDownload> downloads = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ProductCategory> categories = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ProductTag> tags = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ProductImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ProductAttribute> attributes = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @JsonProperty("default_attributes")
    private List<ProductDefaultAttribute> defaultAttributes = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "woo_product_variation_ids", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "variation_id")
    private List<Integer> variations = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "woo_product_grouped_products", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "grouped_product_id")
    @JsonProperty("grouped_products")
    private List<Integer> groupedProducts = new ArrayList<>();

    @Column(name = "menu_order")
    @JsonProperty("menu_order")
    private Integer menuOrder;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @JsonProperty("meta_data")
    private List<ProductMetaData> metaData = new ArrayList<>();

    public WooProduct() {
    }

    // Getters and Setters

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

    public String getPermalink() {
        return permalink;
    }

    public void setPermalink(String permalink) {
        this.permalink = permalink;
    }

    public OffsetDateTime getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(OffsetDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }

    public OffsetDateTime getDateCreatedGmt() {
        return dateCreatedGmt;
    }

    public void setDateCreatedGmt(OffsetDateTime dateCreatedGmt) {
        this.dateCreatedGmt = dateCreatedGmt;
    }

    public OffsetDateTime getDateModified() {
        return dateModified;
    }

    public void setDateModified(OffsetDateTime dateModified) {
        this.dateModified = dateModified;
    }

    public OffsetDateTime getDateModifiedGmt() {
        return dateModifiedGmt;
    }

    public void setDateModifiedGmt(OffsetDateTime dateModifiedGmt) {
        this.dateModifiedGmt = dateModifiedGmt;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getFeatured() {
        return featured;
    }

    public void setFeatured(Boolean featured) {
        this.featured = featured;
    }

    public String getCatalogVisibility() {
        return catalogVisibility;
    }

    public void setCatalogVisibility(String catalogVisibility) {
        this.catalogVisibility = catalogVisibility;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getGlobalUniqueId() {
        return globalUniqueId;
    }

    public void setGlobalUniqueId(String globalUniqueId) {
        this.globalUniqueId = globalUniqueId;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getRegularPrice() {
        return regularPrice;
    }

    public void setRegularPrice(String regularPrice) {
        this.regularPrice = regularPrice;
    }

    public String getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(String salePrice) {
        this.salePrice = salePrice;
    }

    public OffsetDateTime getDateOnSaleFrom() {
        return dateOnSaleFrom;
    }

    public void setDateOnSaleFrom(OffsetDateTime dateOnSaleFrom) {
        this.dateOnSaleFrom = dateOnSaleFrom;
    }

    public OffsetDateTime getDateOnSaleFromGmt() {
        return dateOnSaleFromGmt;
    }

    public void setDateOnSaleFromGmt(OffsetDateTime dateOnSaleFromGmt) {
        this.dateOnSaleFromGmt = dateOnSaleFromGmt;
    }

    public OffsetDateTime getDateOnSaleTo() {
        return dateOnSaleTo;
    }

    public void setDateOnSaleTo(OffsetDateTime dateOnSaleTo) {
        this.dateOnSaleTo = dateOnSaleTo;
    }

    public OffsetDateTime getDateOnSaleToGmt() {
        return dateOnSaleToGmt;
    }

    public void setDateOnSaleToGmt(OffsetDateTime dateOnSaleToGmt) {
        this.dateOnSaleToGmt = dateOnSaleToGmt;
    }

    public String getPriceHtml() {
        return priceHtml;
    }

    public void setPriceHtml(String priceHtml) {
        this.priceHtml = priceHtml;
    }

    public Boolean getOnSale() {
        return onSale;
    }

    public void setOnSale(Boolean onSale) {
        this.onSale = onSale;
    }

    public Boolean getPurchasable() {
        return purchasable;
    }

    public void setPurchasable(Boolean purchasable) {
        this.purchasable = purchasable;
    }

    public Integer getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(Integer totalSales) {
        this.totalSales = totalSales;
    }

    public Boolean getVirtual() {
        return virtual;
    }

    public void setVirtual(Boolean virtual) {
        this.virtual = virtual;
    }

    public Boolean getDownloadable() {
        return downloadable;
    }

    public void setDownloadable(Boolean downloadable) {
        this.downloadable = downloadable;
    }

    public Integer getDownloadLimit() {
        return downloadLimit;
    }

    public void setDownloadLimit(Integer downloadLimit) {
        this.downloadLimit = downloadLimit;
    }

    public Integer getDownloadExpiry() {
        return downloadExpiry;
    }

    public void setDownloadExpiry(Integer downloadExpiry) {
        this.downloadExpiry = downloadExpiry;
    }

    public String getExternalUrl() {
        return externalUrl;
    }

    public void setExternalUrl(String externalUrl) {
        this.externalUrl = externalUrl;
    }

    public String getButtonText() {
        return buttonText;
    }

    public void setButtonText(String buttonText) {
        this.buttonText = buttonText;
    }

    public String getTaxStatus() {
        return taxStatus;
    }

    public void setTaxStatus(String taxStatus) {
        this.taxStatus = taxStatus;
    }

    public String getTaxClass() {
        return taxClass;
    }

    public void setTaxClass(String taxClass) {
        this.taxClass = taxClass;
    }

    public Boolean getManageStock() {
        return manageStock;
    }

    public void setManageStock(Boolean manageStock) {
        this.manageStock = manageStock;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public String getStockStatus() {
        return stockStatus;
    }

    public void setStockStatus(String stockStatus) {
        this.stockStatus = stockStatus;
    }

    public String getBackorders() {
        return backorders;
    }

    public void setBackorders(String backorders) {
        this.backorders = backorders;
    }

    public Boolean getBackordersAllowed() {
        return backordersAllowed;
    }

    public void setBackordersAllowed(Boolean backordersAllowed) {
        this.backordersAllowed = backordersAllowed;
    }

    public Boolean getBackordered() {
        return backordered;
    }

    public void setBackordered(Boolean backordered) {
        this.backordered = backordered;
    }

    public Integer getLowStockAmount() {
        return lowStockAmount;
    }

    public void setLowStockAmount(Integer lowStockAmount) {
        this.lowStockAmount = lowStockAmount;
    }

    public Boolean getSoldIndividually() {
        return soldIndividually;
    }

    public void setSoldIndividually(Boolean soldIndividually) {
        this.soldIndividually = soldIndividually;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public ProductDimensions getDimensions() {
        return dimensions;
    }

    public void setDimensions(ProductDimensions dimensions) {
        this.dimensions = dimensions;
    }

    public Boolean getShippingRequired() {
        return shippingRequired;
    }

    public void setShippingRequired(Boolean shippingRequired) {
        this.shippingRequired = shippingRequired;
    }

    public Boolean getShippingTaxable() {
        return shippingTaxable;
    }

    public void setShippingTaxable(Boolean shippingTaxable) {
        this.shippingTaxable = shippingTaxable;
    }

    public String getShippingClass() {
        return shippingClass;
    }

    public void setShippingClass(String shippingClass) {
        this.shippingClass = shippingClass;
    }

    public Integer getShippingClassId() {
        return shippingClassId;
    }

    public void setShippingClassId(Integer shippingClassId) {
        this.shippingClassId = shippingClassId;
    }

    public Boolean getReviewsAllowed() {
        return reviewsAllowed;
    }

    public void setReviewsAllowed(Boolean reviewsAllowed) {
        this.reviewsAllowed = reviewsAllowed;
    }

    public String getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(String averageRating) {
        this.averageRating = averageRating;
    }

    public Integer getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(Integer ratingCount) {
        this.ratingCount = ratingCount;
    }

    public List<Integer> getRelatedIds() {
        return relatedIds;
    }

    public void setRelatedIds(List<Integer> relatedIds) {
        this.relatedIds = relatedIds;
    }

    public List<Integer> getUpsellIds() {
        return upsellIds;
    }

    public void setUpsellIds(List<Integer> upsellIds) {
        this.upsellIds = upsellIds;
    }

    public List<Integer> getCrossSellIds() {
        return crossSellIds;
    }

    public void setCrossSellIds(List<Integer> crossSellIds) {
        this.crossSellIds = crossSellIds;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public String getPurchaseNote() {
        return purchaseNote;
    }

    public void setPurchaseNote(String purchaseNote) {
        this.purchaseNote = purchaseNote;
    }

    public List<ProductDownload> getDownloads() {
        return downloads;
    }

    public void setDownloads(List<ProductDownload> downloads) {
        this.downloads = downloads;
    }

    public List<ProductCategory> getCategories() {
        return categories;
    }

    public void setCategories(List<ProductCategory> categories) {
        this.categories = categories;
    }

    public List<ProductTag> getTags() {
        return tags;
    }

    public void setTags(List<ProductTag> tags) {
        this.tags = tags;
    }

    public List<ProductImage> getImages() {
        return images;
    }

    public void setImages(List<ProductImage> images) {
        this.images = images;
    }

    public List<ProductAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<ProductAttribute> attributes) {
        this.attributes = attributes;
    }

    public List<ProductDefaultAttribute> getDefaultAttributes() {
        return defaultAttributes;
    }

    public void setDefaultAttributes(List<ProductDefaultAttribute> defaultAttributes) {
        this.defaultAttributes = defaultAttributes;
    }

    public List<Integer> getVariations() {
        return variations;
    }

    public void setVariations(List<Integer> variations) {
        this.variations = variations;
    }

    public List<Integer> getGroupedProducts() {
        return groupedProducts;
    }

    public void setGroupedProducts(List<Integer> groupedProducts) {
        this.groupedProducts = groupedProducts;
    }

    public Integer getMenuOrder() {
        return menuOrder;
    }

    public void setMenuOrder(Integer menuOrder) {
        this.menuOrder = menuOrder;
    }

    public List<ProductMetaData> getMetaData() {
        return metaData;
    }

    public void setMetaData(List<ProductMetaData> metaData) {
        this.metaData = metaData;
    }

    // Helper methods to manage bidirectional relationships

    public void addDownload(ProductDownload download) {
        downloads.add(download);
        download.setProduct(this);
    }

    public void removeDownload(ProductDownload download) {
        downloads.remove(download);
        download.setProduct(null);
    }

    public void addCategory(ProductCategory category) {
        categories.add(category);
        category.setProduct(this);
    }

    public void removeCategory(ProductCategory category) {
        categories.remove(category);
        category.setProduct(null);
    }

    public void addTag(ProductTag tag) {
        tags.add(tag);
        tag.setProduct(this);
    }

    public void removeTag(ProductTag tag) {
        tags.remove(tag);
        tag.setProduct(null);
    }

    public void addImage(ProductImage image) {
        images.add(image);
        image.setProduct(this);
    }

    public void removeImage(ProductImage image) {
        images.remove(image);
        image.setProduct(null);
    }

    public void addAttribute(ProductAttribute attribute) {
        attributes.add(attribute);
        attribute.setProduct(this);
    }

    public void removeAttribute(ProductAttribute attribute) {
        attributes.remove(attribute);
        attribute.setProduct(null);
    }

    public void addDefaultAttribute(ProductDefaultAttribute defaultAttribute) {
        defaultAttributes.add(defaultAttribute);
        defaultAttribute.setProduct(this);
    }

    public void removeDefaultAttribute(ProductDefaultAttribute defaultAttribute) {
        defaultAttributes.remove(defaultAttribute);
        defaultAttribute.setProduct(null);
    }

    public void addMetaData(ProductMetaData meta) {
        metaData.add(meta);
        meta.setProduct(this);
    }

    public void removeMetaData(ProductMetaData meta) {
        metaData.remove(meta);
        meta.setProduct(null);
    }
}
