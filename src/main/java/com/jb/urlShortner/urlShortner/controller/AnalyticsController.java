package com.jb.urlShortner.urlShortner.controller;

import com.jb.urlShortner.urlShortner.domain.AnalyticsData;
import com.jb.urlShortner.urlShortner.domain.AnalyticsRequest;
import com.jb.urlShortner.urlShortner.service.AuthIdentityResolver;
import com.jb.urlShortner.urlShortner.service.AnalyticsService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final AuthIdentityResolver authIdentityResolver;

    public AnalyticsController(AnalyticsService analyticsService,
                               AuthIdentityResolver authIdentityResolver) {
        this.analyticsService = analyticsService;
        this.authIdentityResolver = authIdentityResolver;
    }

    @GetMapping("/urls/my")
    public ResponseEntity<?> getMyUrls(Authentication authentication, HttpServletRequest request) {
        var user = authIdentityResolver.resolve(authentication, request);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Authentication required"));
        }

        List<AnalyticsData> urls = analyticsService.getMyUrls(user.get().login());
        return ResponseEntity.ok(urls);
    }

    @GetMapping("/metrics/summary")
    public ResponseEntity<?> getMyMetrics(Authentication authentication, HttpServletRequest request) {
        var user = authIdentityResolver.resolve(authentication, request);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Authentication required"));
        }

        AnalyticsRequest summary = analyticsService.getMySummary(user.get().login());
        return ResponseEntity.ok(summary);
    }
}
