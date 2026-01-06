package it.alpacode.goldensneakersproxy.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.Base64;

/**
 * Configuration for WooCommerce API client.
 */
@Configuration
@EnableConfigurationProperties(ShopProperties.class)
public class ShopClientConfig {

    private final ShopProperties shopProperties;

    public ShopClientConfig(ShopProperties shopProperties) {
        this.shopProperties = shopProperties;
    }

    @Bean
    public WebClient wooCommerceWebClient() {
        String credentials = shopProperties.getWoocommerce().getConsumerKey()
                + ":" + shopProperties.getWoocommerce().getConsumerSecret();
        String basicAuth = Base64.getEncoder().encodeToString(credentials.getBytes());

        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofMillis(shopProperties.getWoocommerce().getTimeout()));

        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(50 * 1024 * 1024))
                .build();

        return WebClient.builder()
                .baseUrl(shopProperties.getBaseUrl() + shopProperties.getWoocommerce().getApiPath())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + basicAuth)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(strategies)
                .build();
    }
}
