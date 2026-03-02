package com.jb.urlShortner.urlShortner.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jb.urlShortner.urlShortner.domain.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class AuthIdentityResolverTest {

    private AuthIdentityResolver resolver;
    private AuthTokenService tokenService;

    @BeforeEach
    void setUp() {
        tokenService = new AuthTokenService(new ObjectMapper(), "token-secret", 30);
        resolver = new AuthIdentityResolver(tokenService);
    }

    @Test
    void resolveUsesOAuthAuthenticationWhenAvailable() {
        Authentication authentication = Mockito.mock(Authentication.class);
        OAuth2User oauth2User = Mockito.mock(OAuth2User.class);
        when(authentication.getPrincipal()).thenReturn(oauth2User);
        when(authentication.getName()).thenReturn("fallbackName");
        when(oauth2User.getAttribute("login")).thenReturn("alice");
        when(oauth2User.getAttribute("email")).thenReturn("alice@example.com");
        when(oauth2User.getAttribute("name")).thenReturn("Alice");

        Optional<AuthenticatedUser> resolved = resolver.resolve(authentication, new MockHttpServletRequest());

        assertTrue(resolved.isPresent());
        assertEquals("alice", resolved.get().login());
    }

    @Test
    void resolveUsesBearerTokenWhenNoOAuthAuthentication() {
        String token = tokenService.issueToken(new AuthenticatedUser("bob", "bob@example.com", "Bob"));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);

        Optional<AuthenticatedUser> resolved = resolver.resolve(null, request);

        assertTrue(resolved.isPresent());
        assertEquals("bob", resolved.get().login());
    }

    @Test
    void resolveReturnsEmptyForInvalidBearerToken() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid.token");

        Optional<AuthenticatedUser> resolved = resolver.resolve(null, request);

        assertTrue(resolved.isEmpty());
    }

    @Test
    void resolveFallsBackToAuthenticationNameWhenOAuthLoginMissing() {
        Authentication authentication = Mockito.mock(Authentication.class);
        OAuth2User oauth2User = Mockito.mock(OAuth2User.class);
        when(authentication.getPrincipal()).thenReturn(oauth2User);
        when(authentication.getName()).thenReturn("fallbackName");
        when(oauth2User.getAttribute("login")).thenReturn("");
        when(oauth2User.getAttribute("email")).thenReturn("fallback@example.com");
        when(oauth2User.getAttribute("name")).thenReturn("Fallback");
        when(oauth2User.getAttributes()).thenReturn(Map.of());

        Optional<AuthenticatedUser> resolved = resolver.resolve(authentication, new MockHttpServletRequest());

        assertTrue(resolved.isPresent());
        assertEquals("fallbackName", resolved.get().login());
    }
}
