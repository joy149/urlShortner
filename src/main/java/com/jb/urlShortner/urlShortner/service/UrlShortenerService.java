package com.jb.urlShortner.urlShortner.service;

import com.jb.urlShortner.urlShortner.domain.URLCollection;
import com.jb.urlShortner.urlShortner.domain.UrlShorteningRequest;
import com.jb.urlShortner.urlShortner.repository.UrlShortenerRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UrlShortenerService {

    private static final String APP_DNS = "http://localhost:8080/%s";

    private final TinyUrlGenerator tinyUrlGenerator;
    private final UrlShortenerRepository urlShortenerRepository;

    public UrlShortenerService(final TinyUrlGenerator tinyUrlGenerator,
                               final UrlShortenerRepository urlShortenerRepository) {
        this.tinyUrlGenerator = tinyUrlGenerator;
        this.urlShortenerRepository = urlShortenerRepository;
    }

    public String getResolvedUrlValue(final String hashId) {
        Optional<URLCollection> resolvedUrl = urlShortenerRepository.findOneByHashValue(hashId);
        if (resolvedUrl.isPresent()) {
            return resolvedUrl.get().getResolvedUrl();
        }else {
            throw new RuntimeException("Short URL not found");
        }
    }

    public URLCollection getShortenedUrl(@Valid final UrlShorteningRequest request) {
        final String longUrl = request.getLongUrl();
        final String hash = tinyUrlGenerator.generateRandomHash();
        Optional<URLCollection> oneByHash = urlShortenerRepository.findOneByHashValue(hash);
        URLCollection urlCollection = new URLCollection(hash, longUrl);

        if (oneByHash.isEmpty()) {
            urlShortenerRepository.save(urlCollection);
        } else {
            throw new RuntimeException("Hash collision found. Please retry.");
        }
        return urlCollection.toBuilder()
                .hashValue(String.format(APP_DNS, hash))
                .build();
    }
}
