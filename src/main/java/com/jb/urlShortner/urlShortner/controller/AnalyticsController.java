package com.jb.urlShortner.urlShortner.controller;

import com.jb.urlShortner.urlShortner.domain.AnalyticsData;
import com.jb.urlShortner.urlShortner.domain.AnalyticsRequest;
import com.jb.urlShortner.urlShortner.service.AnalyticsService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(final AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @PostMapping
    public ResponseEntity<AnalyticsData> getAnalytics(@RequestBody @Valid final AnalyticsRequest request) {
        AnalyticsData data = analyticsService.getAnalytics(request.shortUrl());
        return ResponseEntity.ok(data);
    }
}
