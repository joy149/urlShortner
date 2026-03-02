package com.jb.urlShortner.urlShortner.controller;

import com.jb.urlShortner.urlShortner.exceptions.DuplicateAliasException;
import com.jb.urlShortner.urlShortner.exceptions.UrlExpirationException;
import com.jb.urlShortner.urlShortner.exceptions.UrlNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handlesUrlNotFoundAs404() {
        ResponseEntity<Map<String, Object>> response = handler.handleUrlNotFoundException(new UrlNotFoundException("x"));
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void handlesDuplicateAliasAs409() {
        ResponseEntity<Map<String, Object>> response = handler.handleDuplicateAliasException(new DuplicateAliasException("dup"));
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void handlesExpirationAs410() {
        ResponseEntity<Map<String, Object>> response = handler.handleUrlExpirationException(new UrlExpirationException(LocalDateTime.now()));
        assertEquals(HttpStatus.GONE, response.getStatusCode());
    }

    @Test
    void handlesOAuthAuthenticationAs401() {
        OAuth2AuthenticationException ex = new OAuth2AuthenticationException(new OAuth2Error("invalid_token"));
        ResponseEntity<Map<String, Object>> response = handler.handleOAuth2AuthenticationException(ex);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void handlesAuthenticationAs401() {
        ResponseEntity<Map<String, Object>> response = handler.handleAuthenticationException(new BadCredentialsException("bad"));
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void handlesGenericExceptionAs500() {
        ResponseEntity<Map<String, Object>> response = handler.handleGenericException(new RuntimeException("boom"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
