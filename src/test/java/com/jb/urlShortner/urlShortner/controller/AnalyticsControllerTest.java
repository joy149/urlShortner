package com.jb.urlShortner.urlShortner.controller;

import com.jb.urlShortner.urlShortner.domain.AnalyticsData;
import com.jb.urlShortner.urlShortner.domain.AnalyticsRequest;
import com.jb.urlShortner.urlShortner.domain.AuthenticatedUser;
import com.jb.urlShortner.urlShortner.service.AnalyticsService;
import com.jb.urlShortner.urlShortner.service.AuthIdentityResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsControllerTest {

    @Mock
    private AnalyticsService analyticsService;
    @Mock
    private AuthIdentityResolver authIdentityResolver;
    @Mock
    private Authentication authentication;
    @Mock
    private HttpServletRequest request;

    private AnalyticsController controller;

    @BeforeEach
    void setUp() {
        controller = new AnalyticsController(analyticsService, authIdentityResolver);
    }

    @Test
    void getMyUrlsReturnsUnauthorizedWhenIdentityMissing() {
        when(authIdentityResolver.resolve(authentication, request)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.getMyUrls(authentication, request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void getMyUrlsReturnsDataForAuthenticatedIdentity() {
        when(authIdentityResolver.resolve(authentication, request))
                .thenReturn(Optional.of(new AuthenticatedUser("alice", "a@x.com", "Alice")));
        when(analyticsService.getMyUrls("alice"))
                .thenReturn(List.of(AnalyticsData.builder()
                        .id("1")
                        .longUrl("https://example.com")
                        .shortUrl("https://sho.rt/abc")
                        .createdAt(LocalDateTime.now())
                        .expirationAt(LocalDateTime.now().plusDays(1))
                        .clickCount(2)
                        .build()));

        ResponseEntity<?> response = controller.getMyUrls(authentication, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getMyMetricsReturnsSummaryForAuthenticatedIdentity() {
        when(authIdentityResolver.resolve(authentication, request))
                .thenReturn(Optional.of(new AuthenticatedUser("alice", "a@x.com", "Alice")));
        when(analyticsService.getMySummary("alice"))
                .thenReturn(AnalyticsRequest.builder()
                        .totalUrls(2)
                        .totalClicks(8)
                        .activeUrls(1)
                        .expiredUrls(1)
                        .build());

        ResponseEntity<?> response = controller.getMyMetrics(authentication, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        AnalyticsRequest body = (AnalyticsRequest) response.getBody();
        assertEquals(2, body.getTotalUrls());
    }
}
