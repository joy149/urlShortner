package com.jb.urlShortner.urlShortner.controller;


import com.jb.urlShortner.urlShortner.domain.URLCollection;
import com.jb.urlShortner.urlShortner.domain.UrlShorteningRequest;
import com.jb.urlShortner.urlShortner.service.UrlShortenerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class UrlShortnerController {

    private final UrlShortenerService urlShortenerService;

    public UrlShortnerController(UrlShortenerService urlShortenerService) {
        this.urlShortenerService = urlShortenerService;
    }

    @GetMapping("/{hashId}")
    public ResponseEntity<?> getResolvedUrl(@PathVariable("hashId") String hashId) {
        String resolvedUrl = urlShortenerService.getResolvedUrlValue(hashId);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", resolvedUrl)
                .build();

    }

    @PostMapping("/shorten")
    public ResponseEntity<URLCollection> getShortenedUrl(@RequestBody @Valid UrlShorteningRequest request) {
        return ResponseEntity.ok(urlShortenerService.getShortenedUrl(request));
    }
}
