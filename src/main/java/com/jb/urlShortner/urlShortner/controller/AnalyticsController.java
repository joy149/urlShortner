package com.jb.urlShortner.urlShortner.controller;

import com.jb.urlShortner.urlShortner.domain.AnalyticsData;
import com.jb.urlShortner.urlShortner.domain.AnalyticsRequest;
import com.jb.urlShortner.urlShortner.service.AnalyticsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/urls/my")
    public ResponseEntity<?> getMyUrls(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof OAuth2User oauth2User)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Authentication required"));
        }

        String ownerLogin = resolveOwnerLogin(authentication, oauth2User);
        List<AnalyticsData> urls = analyticsService.getMyUrls(ownerLogin);
        return ResponseEntity.ok(urls);
    }

    @GetMapping("/metrics/summary")
    public ResponseEntity<?> getMyMetrics(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof OAuth2User oauth2User)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Authentication required"));
        }

        String ownerLogin = resolveOwnerLogin(authentication, oauth2User);
        AnalyticsRequest summary = analyticsService.getMySummary(ownerLogin);
        return ResponseEntity.ok(summary);
    }

    private String resolveOwnerLogin(Authentication authentication, OAuth2User oauth2User) {
        String ownerLogin = oauth2User.getAttribute("login");
        return Objects.nonNull(ownerLogin) && !ownerLogin.isBlank() ? ownerLogin : authentication.getName();
    }
}
