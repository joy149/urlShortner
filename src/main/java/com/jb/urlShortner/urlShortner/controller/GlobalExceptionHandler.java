package com.jb.urlShortner.urlShortner.controller;


import com.jb.urlShortner.urlShortner.exceptions.DuplicateAliasException;
import com.jb.urlShortner.urlShortner.exceptions.UrlNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UrlNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUrlNotFoundException(UrlNotFoundException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(DuplicateAliasException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateAliasException(DuplicateAliasException ex) {
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong!");
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus httpStatus, String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now());
        error.put("status", httpStatus.value());
        error.put("error", httpStatus.getReasonPhrase());
        error.put("message", message);
        return new ResponseEntity<>(error, httpStatus);
    }
}
