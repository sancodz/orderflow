package com.sathiya.order_service.exception;

public class ExternalServiceCommunicationException extends RuntimeException {
    public ExternalServiceCommunicationException(String message) { super(message); }
    public ExternalServiceCommunicationException(String message, Throwable cause) { super(message, cause); }
}