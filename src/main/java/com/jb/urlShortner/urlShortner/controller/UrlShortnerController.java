package com.jb.urlShortner.urlShortner.controller;


import com.jb.urlShortner.urlShortner.domain.UrlShorteningRequest;
import com.jb.urlShortner.urlShortner.service.UrlShortenerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;

@RestController
public class UrlShortnerController {

    private final UrlShortenerService urlShortenerService;

    public UrlShortnerController(UrlShortenerService urlShortenerService) {
        this.urlShortenerService = urlShortenerService;
    }

    @GetMapping("/ping")
    public String ping() {
        return "{ 'status': 'ok' }";
    }

    @GetMapping("/{hashId}")
    public ResponseEntity<?> getResolvedUrl(@PathVariable("hashId") String hashId) {
        String resolvedUrl = urlShortenerService.getResolvedUrlValue(hashId);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", resolvedUrl)
                .build();

    }

    @PostMapping("/shorten")
    public ResponseEntity<?> getShortenedUrl(@RequestBody @Valid UrlShorteningRequest request,
                                             Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof OAuth2User oauth2User)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Authentication required"));
        }

        String ownerLogin = resolveOwnerLogin(authentication, oauth2User);
        String ownerEmail = oauth2User.getAttribute("email");
        return ResponseEntity.ok(urlShortenerService.getShortenedUrl(request, ownerLogin, ownerEmail));
    }

    private String resolveOwnerLogin(Authentication authentication, OAuth2User oauth2User) {
        String ownerLogin = oauth2User.getAttribute("login");
        return Objects.nonNull(ownerLogin) && !ownerLogin.isBlank() ? ownerLogin : authentication.getName();
    }
}
