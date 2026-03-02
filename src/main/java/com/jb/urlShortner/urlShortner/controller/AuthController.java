package com.jb.urlShortner.urlShortner.controller;

import com.jb.urlShortner.urlShortner.domain.AuthenticatedUser;
import com.jb.urlShortner.urlShortner.service.AuthIdentityResolver;
import com.jb.urlShortner.urlShortner.service.AuthTokenService;
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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@RestController
public class AuthController {

    private static final String REDIRECT_URI_SESSION_KEY = "oauth_redirect_uri";

    // fallback kept for cases where property is missing
    private static final String DEFAULT_FRONTEND_FALLBACK = "http://localhost:10001/?postLogin=1";

    private final String defaultFrontendUrlFromConfig;
    private final AuthTokenService authTokenService;
    private final AuthIdentityResolver authIdentityResolver;

    public AuthController(@Value("${frontend.default-url}") String defaultFrontendUrlFromConfig,
                          AuthTokenService authTokenService,
                          AuthIdentityResolver authIdentityResolver) {
        this.defaultFrontendUrlFromConfig = defaultFrontendUrlFromConfig;
        this.authTokenService = authTokenService;
        this.authIdentityResolver = authIdentityResolver;
    }

    private String getDefaultFrontendUrl() {
        return (defaultFrontendUrlFromConfig == null || defaultFrontendUrlFromConfig.isBlank())
                ? DEFAULT_FRONTEND_FALLBACK
                : defaultFrontendUrlFromConfig;
    }

    @GetMapping("/auth/me")
    public ResponseEntity<?> me(Authentication authentication, HttpServletRequest request) {
        Optional<AuthenticatedUser> resolvedUser = authIdentityResolver.resolve(authentication, request);
        if (resolvedUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("authenticated", false));
        }

        AuthenticatedUser user = resolvedUser.get();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("authenticated", true);
        response.put("name", user.name());
        response.put("login", user.login());
        response.put("email", user.email());

        if (authentication != null && authentication.getPrincipal() instanceof OAuth2User oauth2User) {
            response.put("avatarUrl", oauth2User.getAttribute("avatar_url"));
            response.put("attributes", oauth2User.getAttributes());
        } else {
            response.put("avatarUrl", null);
            response.put("attributes", Map.of());
        }
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
    public ResponseEntity<?> success(HttpServletRequest request, Authentication authentication) {
        HttpSession session = request.getSession(false);
        String redirect = getDefaultFrontendUrl();

        if (session != null) {
            Object value = session.getAttribute(REDIRECT_URI_SESSION_KEY);
            if (value instanceof String redirectFromSession && !redirectFromSession.isBlank()) {
                redirect = redirectFromSession;
            }
            session.removeAttribute(REDIRECT_URI_SESSION_KEY);
        }

        if (authentication != null && authentication.getPrincipal() instanceof OAuth2User oauth2User) {
            String ownerLogin = oauth2User.getAttribute("login");
            String login = Objects.nonNull(ownerLogin) && !ownerLogin.isBlank() ? ownerLogin : authentication.getName();
            String token = authTokenService.issueToken(new AuthenticatedUser(
                    login,
                    oauth2User.getAttribute("email"),
                    oauth2User.getAttribute("name")
            ));
            redirect = appendQueryParam(redirect, "token", token);
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

    private String appendQueryParam(String baseUrl, String key, String value) {
        String separator = baseUrl.contains("?") ? "&" : "?";
        return baseUrl + separator + key + "=" + URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
