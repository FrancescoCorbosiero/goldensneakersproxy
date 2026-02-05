package it.alpacode.goldensneakersproxy.entity.woocommerce;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "woo_variations")
public class WooVariation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id")
    @JsonProperty("product_id")
    private Long productId;

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

    @Column(columnDefinition = "TEXT")
    private String description;

    private String permalink;

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

    @Column(name = "on_sale")
    @JsonProperty("on_sale")
    private Boolean onSale;

    private String status;

    private Boolean purchasable;

    private Boolean virtual;

    private Boolean downloadable;

    @Column(name = "download_limit")
    @JsonProperty("download_limit")
    private Integer downloadLimit;

    @Column(name = "download_expiry")
    @JsonProperty("download_expiry")
    private Integer downloadExpiry;

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

    private String weight;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "length", column = @Column(name = "dimension_length")),
        @AttributeOverride(name = "width", column = @Column(name = "dimension_width")),
        @AttributeOverride(name = "height", column = @Column(name = "dimension_height"))
    })
    private ProductDimensions dimensions;

    @Column(name = "shipping_class")
    @JsonProperty("shipping_class")
    private String shippingClass;

    @Column(name = "shipping_class_id")
    @JsonProperty("shipping_class_id")
    private Integer shippingClassId;

    @Embedded
    private VariationImage image;

    @Column(name = "menu_order")
    @JsonProperty("menu_order")
    private Integer menuOrder;

    @OneToMany(mappedBy = "variation", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<VariationDownload> downloads = new ArrayList<>();

    @OneToMany(mappedBy = "variation", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<VariationAttribute> attributes = new ArrayList<>();

    @OneToMany(mappedBy = "variation", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @JsonProperty("meta_data")
    private List<VariationMetaData> metaData = new ArrayList<>();

    public WooVariation() {
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPermalink() {
        return permalink;
    }

    public void setPermalink(String permalink) {
        this.permalink = permalink;
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

    public Boolean getOnSale() {
        return onSale;
    }

    public void setOnSale(Boolean onSale) {
        this.onSale = onSale;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getPurchasable() {
        return purchasable;
    }

    public void setPurchasable(Boolean purchasable) {
        this.purchasable = purchasable;
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

    public VariationImage getImage() {
        return image;
    }

    public void setImage(VariationImage image) {
        this.image = image;
    }

    public Integer getMenuOrder() {
        return menuOrder;
    }

    public void setMenuOrder(Integer menuOrder) {
        this.menuOrder = menuOrder;
    }

    public List<VariationDownload> getDownloads() {
        return downloads;
    }

    public void setDownloads(List<VariationDownload> downloads) {
        this.downloads = downloads;
    }

    public List<VariationAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<VariationAttribute> attributes) {
        this.attributes = attributes;
    }

    public List<VariationMetaData> getMetaData() {
        return metaData;
    }

    public void setMetaData(List<VariationMetaData> metaData) {
        this.metaData = metaData;
    }

    // Helper methods to manage bidirectional relationships

    public void addDownload(VariationDownload download) {
        downloads.add(download);
        download.setVariation(this);
    }

    public void removeDownload(VariationDownload download) {
        downloads.remove(download);
        download.setVariation(null);
    }

    public void addAttribute(VariationAttribute attribute) {
        attributes.add(attribute);
        attribute.setVariation(this);
    }

    public void removeAttribute(VariationAttribute attribute) {
        attributes.remove(attribute);
        attribute.setVariation(null);
    }

    public void addMetaData(VariationMetaData meta) {
        metaData.add(meta);
        meta.setVariation(this);
    }

    public void removeMetaData(VariationMetaData meta) {
        metaData.remove(meta);
        meta.setVariation(null);
    }
}
