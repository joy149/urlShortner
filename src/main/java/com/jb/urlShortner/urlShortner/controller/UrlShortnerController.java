package com.jb.urlShortner.urlShortner.controller;


import com.jb.urlShortner.urlShortner.domain.UrlShorteningRequest;
import com.jb.urlShortner.urlShortner.service.AuthIdentityResolver;
import com.jb.urlShortner.urlShortner.service.UrlShortenerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
public class UrlShortnerController {

    private final UrlShortenerService urlShortenerService;
    private final AuthIdentityResolver authIdentityResolver;

    public UrlShortnerController(UrlShortenerService urlShortenerService,
                                 AuthIdentityResolver authIdentityResolver) {
        this.urlShortenerService = urlShortenerService;
        this.authIdentityResolver = authIdentityResolver;
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
                                             Authentication authentication,
                                             HttpServletRequest httpServletRequest) {
        return authIdentityResolver.resolve(authentication, httpServletRequest)
                .map(user -> ResponseEntity.ok(urlShortenerService.getShortenedUrl(request, user.login(), user.email())))
                .orElseGet(() -> ResponseEntity.ok(urlShortenerService.getShortenedUrl(request, null, null)));
    }
}
