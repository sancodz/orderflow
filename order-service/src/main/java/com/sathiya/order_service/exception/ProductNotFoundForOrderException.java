package com.sathiya.order_service.exception;

public class ProductNotFoundForOrderException extends RuntimeException {
    public ProductNotFoundForOrderException(String message) { super(message); }
}