package com.sathiya.order_service.entity;

public enum OrderStatus {
    PENDING,        // Order created, awaiting payment/processing
    PROCESSING,     // Payment successful, order being processed
    SHIPPED,
    DELIVERED,
    CANCELLED,
    FAILED          // e.g. Payment failed, or inventory issue post-check
}
