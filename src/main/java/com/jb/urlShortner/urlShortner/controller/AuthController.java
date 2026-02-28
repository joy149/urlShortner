package com.jb.urlShortner.urlShortner.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class AuthController {

    private static final String REDIRECT_URI_SESSION_KEY = "oauth_redirect_uri";

    // fallback kept for cases where property is missing
    private static final String DEFAULT_FRONTEND_FALLBACK = "http://localhost:10001/?postLogin=1";

    private final String defaultFrontendUrlFromConfig;

    public AuthController(@Value("${frontend.default-url:}") String defaultFrontendUrlFromConfig) {
        this.defaultFrontendUrlFromConfig = defaultFrontendUrlFromConfig;
    }

    private String getDefaultFrontendUrl() {
        return (defaultFrontendUrlFromConfig == null || defaultFrontendUrlFromConfig.isBlank())
                ? DEFAULT_FRONTEND_FALLBACK
                : defaultFrontendUrlFromConfig;
    }

    @GetMapping("/auth/me")
    public ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof OAuth2User oauth2User)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("authenticated", false));
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("authenticated", true);
        response.put("name", oauth2User.getAttribute("name"));
        response.put("login", oauth2User.getAttribute("login"));
        response.put("email", oauth2User.getAttribute("email"));
        response.put("avatarUrl", oauth2User.getAttribute("avatar_url"));
        response.put("attributes", oauth2User.getAttributes());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/auth/login")
    public ResponseEntity<?> login(
            @RequestParam(value = "provider", required = false, defaultValue = "github") String provider,
            @RequestParam(value = "redirectUri", required = false) String redirectUri,
            @RequestParam(value = "redirect_uri", required = false) String redirectUriSnake,
            HttpServletRequest request
    ) {
        HttpSession session = request.getSession(true);
        String resolvedRedirect = redirectUri != null && !redirectUri.isBlank()
                ? redirectUri
                : (redirectUriSnake != null && !redirectUriSnake.isBlank() ? redirectUriSnake : getDefaultFrontendUrl());
        session.setAttribute(REDIRECT_URI_SESSION_KEY, resolvedRedirect);

        String registrationId = "google".equalsIgnoreCase(provider) ? "google" : "github";

        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", "/oauth2/authorization/" + registrationId)
                .build();
    }

    @GetMapping("/auth/success")
    public ResponseEntity<?> success(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        String redirect = getDefaultFrontendUrl();

        if (session != null) {
            Object value = session.getAttribute(REDIRECT_URI_SESSION_KEY);
            if (value instanceof String redirectFromSession && !redirectFromSession.isBlank()) {
                redirect = redirectFromSession;
            }
            session.removeAttribute(REDIRECT_URI_SESSION_KEY);
        }

        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", redirect)
                .build();
    }

    @GetMapping("/auth/failure")
    public ResponseEntity<?> failure(
            @RequestParam(value = "error", required = false) String error,
            HttpServletRequest request
    ) {
        HttpSession session = request.getSession(false);
        String redirect = getDefaultFrontendUrl();

        if (session != null) {
            Object value = session.getAttribute(REDIRECT_URI_SESSION_KEY);
            if (value instanceof String redirectFromSession && !redirectFromSession.isBlank()) {
                redirect = redirectFromSession;
            }
            session.removeAttribute(REDIRECT_URI_SESSION_KEY);
        }

        String separator = redirect.contains("?") ? "&" : "?";
        String target = redirect + separator + "authError=" + (error == null ? "oauth_login_failed" : error);

        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", target)
                .build();
    }
}
