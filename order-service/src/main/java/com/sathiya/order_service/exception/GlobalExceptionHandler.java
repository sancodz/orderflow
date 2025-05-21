package com.sathiya.order_service.exception;

import com.sathiya.order_service.dto.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleOrderNotFoundException(OrderNotFoundException ex, HttpServletRequest request) {
        log.warn("OrderNotFoundException: {} for path: {}", ex.getMessage(), request.getRequestURI());
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(ProductNotFoundForOrderException.class)
    public ResponseEntity<ErrorResponseDTO> handleProductNotFoundForOrderException(ProductNotFoundForOrderException ex, HttpServletRequest request) {
        log.warn("ProductNotFoundForOrderException: {} for path: {}", ex.getMessage(), request.getRequestURI());
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, request, "Product Not Found"); // Or NOT_FOUND depending on context
    }

    @ExceptionHandler(InsufficientStockForOrderException.class)
    public ResponseEntity<ErrorResponseDTO> handleInsufficientStockForOrderException(InsufficientStockForOrderException ex, HttpServletRequest request) {
        log.warn("InsufficientStockForOrderException: {} for path: {}", ex.getMessage(), request.getRequestURI());
        return buildErrorResponse(ex, HttpStatus.CONFLICT, request, "Insufficient Stock"); // 409 Conflict for resource state
    }

    @ExceptionHandler(ExternalServiceCommunicationException.class)
    public ResponseEntity<ErrorResponseDTO> handleExternalServiceCommunicationException(ExternalServiceCommunicationException ex, HttpServletRequest request) {
        log.error("ExternalServiceCommunicationException: {} for path: {}", ex.getMessage(), request.getRequestURI(), ex.getCause());
        return buildErrorResponse(ex, HttpStatus.SERVICE_UNAVAILABLE, request, "External Service Error");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> details = ex.getBindingResult().getAllErrors().stream()
                .map(error -> (error instanceof FieldError) ? ((FieldError) error).getField() + ": " + error.getDefaultMessage() : error.getDefaultMessage())
                .collect(Collectors.toList());
        log.warn("MethodArgumentNotValidException: Validation failed for path: {} with details: {}", request.getRequestURI(), details);
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(), "Validation Failed",
                "Input validation failed.", request.getRequestURI(), details);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("IllegalArgumentException: {} for path: {}", ex.getMessage(), request.getRequestURI());
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error occurred at path: {}", request.getRequestURI(), ex);
        return buildErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, request, "An unexpected internal server error occurred.");
    }

    private ResponseEntity<ErrorResponseDTO> buildErrorResponse(Exception ex, HttpStatus status, HttpServletRequest request) {
        return buildErrorResponse(ex, status, request, status.getReasonPhrase());
    }

    private ResponseEntity<ErrorResponseDTO> buildErrorResponse(Exception ex, HttpStatus status, HttpServletRequest request, String errorType) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                LocalDateTime.now(),
                status.value(),
                errorType,
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, status);
    }
}