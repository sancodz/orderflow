package com.sathiya.order_service.service.client;

import com.sathiya.order_service.dto.external.ProductDTO;
import com.sathiya.order_service.exception.ExternalServiceCommunicationException;
import com.sathiya.order_service.exception.ProductNotFoundForOrderException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient; // Import RestClient

@Component
@Slf4j
public class ProductServiceClient {

    private final RestClient restClient;

    public ProductServiceClient(RestClient.Builder restClientBuilder,
                                @Value("${external.service.product.url}") String productServiceUrl) {
        this.restClient = restClientBuilder
                .baseUrl(productServiceUrl)
                .build();
    }

    public ProductDTO getProductBySku(String sku) {
        String path = "/sku/" + sku;
        log.info("Calling Product Service: GET {}{}", this.restClient.toString(), path); // toString shows base URL
        try {
            return restClient.get()
                    .uri(path)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        log.error("Client error from Product Service for SKU {}: {} - {}", sku, response.getStatusCode(), response.getStatusText());
                        if (response.getStatusCode().value() == 404) {
                            throw new ProductNotFoundForOrderException("Product with SKU " + sku + " not found (404).");
                        }
                        // For other 4xx errors, extract body if possible for more details
                        String responseBody = "";
                        try {
                            responseBody = new String(response.getBody().readAllBytes());
                        } catch (Exception ignored) {}
                        throw new ExternalServiceCommunicationException(
                                "Product Service client error: " + response.getStatusCode() + " - " + responseBody);
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                        log.error("Server error from Product Service for SKU {}: {} - {}", sku, response.getStatusCode(), response.getStatusText());
                        String responseBody = "";
                        try {
                            responseBody = new String(response.getBody().readAllBytes());
                        } catch (Exception ignored) {}
                        throw new ExternalServiceCommunicationException(
                                "Product Service server error: " + response.getStatusCode() + " - " + responseBody);
                    })
                    .body(ProductDTO.class);

        } catch (ResourceAccessException e) { // Covers connection timeouts, DNS issues etc.
            log.error("ResourceAccessException (e.g. connection timeout) while calling Product Service for SKU {}: {}", sku, e.getMessage(), e);
            throw new ExternalServiceCommunicationException("Could not connect to Product Service: " + e.getMessage(), e);
        } catch (ProductNotFoundForOrderException e) { // Re-throw specific exception
            log.warn("Product not found in Product Service for SKU: {}", sku);
            throw e;
        } catch (Exception e) { // Catch-all for other RestClient or unexpected issues
            log.error("Unexpected error while calling Product Service for SKU {}: {}", sku, e.getMessage(), e);
            throw new ExternalServiceCommunicationException("Unexpected error fetching product for SKU " + sku + ": " + e.getMessage(), e);
        }
    }
}