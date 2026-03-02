package com.jb.urlShortner.urlShortner.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jb.urlShortner.urlShortner.domain.AuthenticatedUser;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthTokenServiceTest {

    @Test
    void issueAndParseTokenSuccess() {
        AuthTokenService tokenService = new AuthTokenService(new ObjectMapper(), "very-secret-value", 30);
        AuthenticatedUser user = new AuthenticatedUser("alice", "alice@example.com", "Alice");

        String token = tokenService.issueToken(user);
        Optional<AuthenticatedUser> parsed = tokenService.parseToken(token);

        assertTrue(parsed.isPresent());
        assertEquals("alice", parsed.get().login());
        assertEquals("alice@example.com", parsed.get().email());
        assertEquals("Alice", parsed.get().name());
    }

    @Test
    void parseTokenReturnsEmptyForInvalidSignature() {
        AuthTokenService tokenService = new AuthTokenService(new ObjectMapper(), "very-secret-value", 30);
        AuthenticatedUser user = new AuthenticatedUser("alice", "alice@example.com", "Alice");
        String token = tokenService.issueToken(user);
        String tampered = token + "x";

        Optional<AuthenticatedUser> parsed = tokenService.parseToken(tampered);
        assertTrue(parsed.isEmpty());
    }

    @Test
    void parseTokenReturnsEmptyForExpiredToken() {
        AuthTokenService tokenService = new AuthTokenService(new ObjectMapper(), "very-secret-value", -1);
        AuthenticatedUser user = new AuthenticatedUser("alice", "alice@example.com", "Alice");
        String token = tokenService.issueToken(user);

        Optional<AuthenticatedUser> parsed = tokenService.parseToken(token);
        assertTrue(parsed.isEmpty());
    }
}
