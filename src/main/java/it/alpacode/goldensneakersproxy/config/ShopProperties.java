package it.alpacode.goldensneakersproxy.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for WooCommerce/WordPress shop integration.
 */
@ConfigurationProperties(prefix = "shop")
public class ShopProperties {

    private String baseUrl;
    private WooCommerceProperties woocommerce = new WooCommerceProperties();
    private WordPressProperties wordpress = new WordPressProperties();
    private SyncProperties sync = new SyncProperties();

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public WooCommerceProperties getWoocommerce() {
        return woocommerce;
    }

    public void setWoocommerce(WooCommerceProperties woocommerce) {
        this.woocommerce = woocommerce;
    }

    public WordPressProperties getWordpress() {
        return wordpress;
    }

    public void setWordpress(WordPressProperties wordpress) {
        this.wordpress = wordpress;
    }

    public SyncProperties getSync() {
        return sync;
    }

    public void setSync(SyncProperties sync) {
        this.sync = sync;
    }

    public static class WooCommerceProperties {
        private String apiPath = "/wp-json/wc/v3";
        private String consumerKey;
        private String consumerSecret;
        private int batchSize = 100;
        private int timeout = 60000;

        public String getApiPath() {
            return apiPath;
        }

        public void setApiPath(String apiPath) {
            this.apiPath = apiPath;
        }

        public String getConsumerKey() {
            return consumerKey;
        }

        public void setConsumerKey(String consumerKey) {
            this.consumerKey = consumerKey;
        }

        public String getConsumerSecret() {
            return consumerSecret;
        }

        public void setConsumerSecret(String consumerSecret) {
            this.consumerSecret = consumerSecret;
        }

        public int getBatchSize() {
            return batchSize;
        }

        public void setBatchSize(int batchSize) {
            this.batchSize = batchSize;
        }

        public int getTimeout() {
            return timeout;
        }

        public void setTimeout(int timeout) {
            this.timeout = timeout;
        }
    }

    public static class WordPressProperties {
        private String apiPath = "/wp-json/wp/v2";

        public String getApiPath() {
            return apiPath;
        }

        public void setApiPath(String apiPath) {
            this.apiPath = apiPath;
        }
    }

    public static class SyncProperties {
        private boolean parallelVariations = true;
        private int maxThreads = 10;
        private boolean markMissingOutOfStock = true;
        private int rateLimitDelayMs = 2000;

        public boolean isParallelVariations() {
            return parallelVariations;
        }

        public void setParallelVariations(boolean parallelVariations) {
            this.parallelVariations = parallelVariations;
        }

        public int getMaxThreads() {
            return maxThreads;
        }

        public void setMaxThreads(int maxThreads) {
            this.maxThreads = maxThreads;
        }

        public boolean isMarkMissingOutOfStock() {
            return markMissingOutOfStock;
        }

        public void setMarkMissingOutOfStock(boolean markMissingOutOfStock) {
            this.markMissingOutOfStock = markMissingOutOfStock;
        }

        public int getRateLimitDelayMs() {
            return rateLimitDelayMs;
        }

        public void setRateLimitDelayMs(int rateLimitDelayMs) {
            this.rateLimitDelayMs = rateLimitDelayMs;
        }
    }
}
