package com.jb.urlShortner.urlShortner.controller;

import com.jb.urlShortner.urlShortner.domain.AuthenticatedUser;
import com.jb.urlShortner.urlShortner.domain.URLCollection;
import com.jb.urlShortner.urlShortner.domain.UrlShorteningRequest;
import com.jb.urlShortner.urlShortner.service.AuthIdentityResolver;
import com.jb.urlShortner.urlShortner.service.UrlShortenerService;
import jakarta.servlet.http.HttpServletRequest;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UrlShortnerControllerTest {

    @Mock
    private UrlShortenerService urlShortenerService;
    @Mock
    private AuthIdentityResolver authIdentityResolver;
    @Mock
    private Authentication authentication;
    @Mock
    private HttpServletRequest request;

    private UrlShortnerController controller;

    @BeforeEach
    void setUp() {
        controller = new UrlShortnerController(urlShortenerService, authIdentityResolver);
    }

    @Test
    void getResolvedUrlReturnsFoundRedirect() {
        when(urlShortenerService.getResolvedUrlValue("abc1234")).thenReturn("https://example.com");

        ResponseEntity<?> response = controller.getResolvedUrl("abc1234");

        assertEquals(HttpStatus.FOUND, response.getStatusCode());
        assertEquals("https://example.com", response.getHeaders().getFirst("Location"));
    }

    @Test
    void getShortenedUrlUsesAuthenticatedIdentityWhenPresent() {
        UrlShorteningRequest payload = UrlShorteningRequest.builder().longUrl("https://example.com").build();
        when(authIdentityResolver.resolve(authentication, request))
                .thenReturn(Optional.of(new AuthenticatedUser("alice", "alice@example.com", "Alice")));
        when(urlShortenerService.getShortenedUrl(eq(payload), eq("alice"), eq("alice@example.com")))
                .thenReturn(urlRecord("abc1234"));

        ResponseEntity<?> response = controller.getShortenedUrl(payload, authentication, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(urlShortenerService).getShortenedUrl(payload, "alice", "alice@example.com");
    }

    @Test
    void getShortenedUrlAllowsAnonymousRequests() {
        UrlShorteningRequest payload = UrlShorteningRequest.builder().longUrl("https://example.com").build();
        when(authIdentityResolver.resolve(authentication, request)).thenReturn(Optional.empty());
        when(urlShortenerService.getShortenedUrl(eq(payload), isNull(), isNull())).thenReturn(urlRecord("anon123"));

        ResponseEntity<?> response = controller.getShortenedUrl(payload, authentication, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(urlShortenerService).getShortenedUrl(payload, null, null);
    }

    private URLCollection urlRecord(String hash) {
        return URLCollection.builder()
                .id(new ObjectId())
                .hashValue(hash)
                .resolvedUrl("https://example.com")
                .createdDate(LocalDateTime.now())
                .expirationDate(LocalDateTime.now().plusDays(7))
                .clickCount(0)
                .build();
    }
}
