package com.jb.urlShortner.urlShortner.service;

import com.jb.urlShortner.urlShortner.domain.URLCollection;
import com.jb.urlShortner.urlShortner.domain.UrlShorteningRequest;
import com.jb.urlShortner.urlShortner.exceptions.DuplicateAliasException;
import com.jb.urlShortner.urlShortner.exceptions.UrlExpirationException;
import com.jb.urlShortner.urlShortner.exceptions.UrlNotFoundException;
import com.jb.urlShortner.urlShortner.repository.UrlShortenerRepository;
import jakarta.validation.Valid;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

@Service
public class UrlShortenerService {

    private final String appDns;
    private final TinyUrlGenerator tinyUrlGenerator;
    private final UrlShortenerRepository urlShortenerRepository;
    private final ObjectIdGenerator idGenerator;

    public UrlShortenerService(final TinyUrlGenerator tinyUrlGenerator,
                               final UrlShortenerRepository urlShortenerRepository,
                               final ObjectIdGenerator idGenerator,
                               final @Value("${application.baseUrl}") String appDns) {
        this.tinyUrlGenerator = tinyUrlGenerator;
        this.urlShortenerRepository = urlShortenerRepository;
        this.idGenerator = idGenerator;
        this.appDns = appDns;
    }

    public String getResolvedUrlValue(final String hashId) {
        final Optional<URLCollection> resolvedUrl = urlShortenerRepository.findOneByHashValue(hashId);
        if (resolvedUrl.isPresent()) {
            final URLCollection urlCollection = resolvedUrl.get();
            if (urlCollection.getExpirationDate().isBefore(LocalDateTime.now())) {
                throw new UrlExpirationException(urlCollection.getExpirationDate());
            }
            final URLCollection buildWithIncreasedClick = urlCollection.toBuilder()
                    .clickCount(urlCollection.getClickCount() + 1)
                    .build();
            urlShortenerRepository.save(buildWithIncreasedClick);
            return urlCollection.getResolvedUrl();
        } else {
            throw new UrlNotFoundException(hashId);
        }
    }

    public URLCollection getShortenedUrl(@Valid final UrlShorteningRequest request) {
        final LocalDateTime now = LocalDateTime.now();
        // Check if we already have seen this url before and is not expired
        final Optional<URLCollection> existing = urlShortenerRepository.findOneByActiveResolvedUrl(request.getLongUrl(), now);
        if (existing.isPresent()) {
            return existing.get().toBuilder()
                    .hashValue(String.format(appDns.concat("%s"), existing.get().getHashValue()))
                    .build();
        }
        final String longUrl = request.getLongUrl();
        final String hash = tinyUrlGenerator.generateRandomHash();
        final Optional<URLCollection> oneByHash = urlShortenerRepository.findOneByHashValue(hash);
        final URLCollection urlCollection = URLCollection.builder()
                .id(idGenerator.getObjectId())
                .hashValue(hash)
                .resolvedUrl(longUrl)
                .createdDate(now)
                .expirationDate(Objects.nonNull(request.getExpirationInDays())
                        ? now.plusDays(request.getExpirationInDays())
                        : now.plusDays(14))
                .clickCount(0)
                .build();

        if (oneByHash.isEmpty()) {
            urlShortenerRepository.save(urlCollection);
        } else {
            final URLCollection existingHash = oneByHash.get();
            if (existingHash.getExpirationDate().isBefore(now)) {
                //reuse the hash
                final URLCollection reuseHash = existingHash.toBuilder()
                        .resolvedUrl(longUrl)
                        .createdDate(now)
                        .expirationDate(now.plusDays(14))
                        .clickCount(0)
                        .build();
                urlShortenerRepository.save(reuseHash);
            } else {
                throw new DuplicateAliasException(hash);
            }
        }
        return urlCollection.toBuilder()
                .hashValue(String.format(appDns.concat("%s"), hash))
                .build();
    }
}
