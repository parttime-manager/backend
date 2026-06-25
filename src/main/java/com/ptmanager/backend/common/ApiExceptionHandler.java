package com.ptmanager.backend.common;

import java.time.Instant;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiError> handleNotFound(NoSuchElementException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ApiError("NOT_FOUND", exception.getMessage(), Instant.now(), Map.of()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleBadRequest(IllegalArgumentException exception) {
        return ResponseEntity.badRequest()
            .body(new ApiError("BAD_REQUEST", exception.getMessage(), Instant.now(), Map.of()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception) {
        Map<String, String> fields = exception.getBindingResult()
            .getFieldErrors()
            .stream()
            .collect(
                java.util.stream.Collectors.toMap(
                    error -> error.getField(),
                    error -> error.getDefaultMessage() == null ? "invalid" : error.getDefaultMessage(),
                    (left, right) -> left
                )
            );

        return ResponseEntity.badRequest()
            .body(new ApiError("VALIDATION_FAILED", "Request validation failed.", Instant.now(), fields));
    }

    public record ApiError(
        String code,
        String message,
        Instant timestamp,
        Map<String, String> fields
    ) {
    }
}
