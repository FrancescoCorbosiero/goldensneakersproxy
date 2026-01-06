package it.alpacode.goldensneakersproxy.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for WooCommerce shop integration.
 */
@ConfigurationProperties(prefix = "shop")
public class ShopProperties {

    private String baseUrl;
    private WooCommerceProperties woocommerce = new WooCommerceProperties();

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

    public static class WooCommerceProperties {
        private String apiPath = "/wp-json/wc/v3";
        private String consumerKey;
        private String consumerSecret;
        private int batchSize = 100;
        private int timeout = 60000;
        private int rateLimitDelayMs = 2000;

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

        public int getRateLimitDelayMs() {
            return rateLimitDelayMs;
        }

        public void setRateLimitDelayMs(int rateLimitDelayMs) {
            this.rateLimitDelayMs = rateLimitDelayMs;
        }
    }
}
