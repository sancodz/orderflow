package com.sathiya.order_service.service.client;

import com.sathiya.order_service.dto.external.InventoryDTO;
import com.sathiya.order_service.exception.ExternalServiceCommunicationException;
import com.sathiya.order_service.exception.InsufficientStockForOrderException;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient; // Import RestClient

@Component
@Slf4j
public class InventoryServiceClient {

    private final RestClient restClient;

    // DTO for decrement request body of Inventory Service
    @Data
    @NoArgsConstructor
    private static class StockAdjustmentRequest {
        private Integer amount;
        public StockAdjustmentRequest(Integer amount) { this.amount = amount; }
    }

    public InventoryServiceClient(RestClient.Builder restClientBuilder,
                                  @Value("${external.service.inventory.url}") String inventoryServiceUrl) {
        this.restClient = restClientBuilder
                .baseUrl(inventoryServiceUrl)
                .build();
    }

    public InventoryDTO getInventoryBySku(String sku) {
        String path = "/sku/" + sku;
        log.info("Calling Inventory Service: GET {}{}", this.restClient.toString(), path);
        try {
            InventoryDTO inventory = restClient.get()
                    .uri(path)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .onStatus(status -> status.value() == 404, (request, response) -> {
                        log.warn("Inventory item not found in Inventory Service for SKU: {} (404)", sku);
                        // This could mean the product has never had stock or is out of stock.
                        throw new InsufficientStockForOrderException("Product with SKU " + sku + " has no inventory record or is out of stock.");
                    })
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        log.error("Client error from Inventory Service (GET) for SKU {}: {} - {}", sku, response.getStatusCode(), response.getStatusText());
                        String responseBody = ""; try { responseBody = new String(response.getBody().readAllBytes()); } catch (Exception ignored) {}
                        throw new ExternalServiceCommunicationException(
                                "Inventory Service client error (GET): " + response.getStatusCode() + " - " + responseBody);
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                        log.error("Server error from Inventory Service (GET) for SKU {}: {} - {}", sku, response.getStatusCode(), response.getStatusText());
                        String responseBody = ""; try { responseBody = new String(response.getBody().readAllBytes()); } catch (Exception ignored) {}
                        throw new ExternalServiceCommunicationException(
                                "Inventory Service server error (GET): " + response.getStatusCode() + " - " + responseBody);
                    })
                    .body(InventoryDTO.class);

            if (inventory != null) {
                log.info("Successfully fetched inventory for SKU: {}. Quantity: {}", sku, inventory.getQuantity());
            }
            return inventory;

        } catch (ResourceAccessException e) {
            log.error("ResourceAccessException while fetching inventory for SKU {}: {}", sku, e.getMessage(), e);
            throw new ExternalServiceCommunicationException("Could not connect to Inventory Service (GET): " + e.getMessage(), e);
        } catch (InsufficientStockForOrderException e) { // Re-throw specific exception
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while fetching inventory for SKU {}: {}", sku, e.getMessage(), e);
            throw new ExternalServiceCommunicationException("Unexpected error fetching inventory for SKU " + sku + ": " + e.getMessage(), e);
        }
    }

    public void decrementStock(String sku, int quantity) {
        String path = "/sku/" + sku + "/decrement";
        log.info("Calling Inventory Service: PATCH {}{} with quantity {}", this.restClient.toString(), path, quantity);
        try {
            StockAdjustmentRequest requestBody = new StockAdjustmentRequest(quantity);

            restClient.patch()
                    .uri(path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .onStatus(status -> status.value() == 400 || status.value() == 409, (request, response) -> { // 400 for bad request (e.g. insufficient stock), 409 for conflict
                        String responseBody = "";
                        try {
                            responseBody = new String(response.getBody().readAllBytes());
                        } catch (Exception ignored) {}
                        log.warn("Failed to decrement stock for SKU {} due to client error {}: {}", sku, response.getStatusCode(), responseBody);
                        if (responseBody.toLowerCase().contains("insufficient stock")) {
                            throw new InsufficientStockForOrderException("Insufficient stock for SKU " + sku + ". Available quantity is less than requested.");
                        }
                        throw new ExternalServiceCommunicationException(
                                "Inventory Service client error (decrement): " + response.getStatusCode() + " - " + responseBody);
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                        log.error("Server error from Inventory Service (decrement) for SKU {}: {} - {}", sku, response.getStatusCode(), response.getStatusText());
                        String responseBody = ""; try { responseBody = new String(response.getBody().readAllBytes()); } catch (Exception ignored) {}
                        throw new ExternalServiceCommunicationException(
                                "Inventory Service server error (decrement): " + response.getStatusCode() + " - " + responseBody);
                    })
                    .toBodilessEntity(); // If no response body is expected or needed for success

            log.info("Successfully decremented stock for SKU: {} by quantity: {}", sku, quantity);

        } catch (ResourceAccessException e) {
            log.error("ResourceAccessException while decrementing stock for SKU {}: {}", sku, e.getMessage(), e);
            throw new ExternalServiceCommunicationException("Could not connect to Inventory Service (decrement): " + e.getMessage(), e);
        } catch (InsufficientStockForOrderException e) { // Re-throw specific exception
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while decrementing stock for SKU {}: {}", sku, e.getMessage(), e);
            throw new ExternalServiceCommunicationException("Unexpected error decrementing stock for SKU " + sku + ": " + e.getMessage(), e);
        }
    }
}