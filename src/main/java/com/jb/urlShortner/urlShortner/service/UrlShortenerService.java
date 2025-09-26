package com.jb.urlShortner.urlShortner.service;

import com.jb.urlShortner.urlShortner.domain.URLCollection;
import com.jb.urlShortner.urlShortner.domain.UrlShorteningRequest;
import com.jb.urlShortner.urlShortner.repository.UrlShortenerRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UrlShortenerService {

    private final String appDns;

    private final TinyUrlGenerator tinyUrlGenerator;
    private final UrlShortenerRepository urlShortenerRepository;

    public UrlShortenerService(final TinyUrlGenerator tinyUrlGenerator,
                               final UrlShortenerRepository urlShortenerRepository,
                               final @Value("${application.baseUrl}") String appDns) {
        this.tinyUrlGenerator = tinyUrlGenerator;
        this.urlShortenerRepository = urlShortenerRepository;
        this.appDns = appDns;
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
        Optional<URLCollection> existing = urlShortenerRepository.findOneByResolvedUrl(request.getLongUrl());
        if (existing.isPresent()) {
            return existing.get().toBuilder()
                    .hashValue(String.format(appDns.concat("%s"), existing.get().getHashValue()))
                    .build();
        }
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
                .hashValue(String.format(appDns.concat("%s"), hash))
                .build();
    }
}
