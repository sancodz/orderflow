package com.sathiya.order_service.exception;

public class InsufficientStockForOrderException extends RuntimeException {
    public InsufficientStockForOrderException(String message) { super(message); }
}