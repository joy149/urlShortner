package com.jb.urlShortner.urlShortner.service;

import com.jb.urlShortner.urlShortner.domain.AnalyticsData;
import com.jb.urlShortner.urlShortner.domain.URLCollection;
import com.jb.urlShortner.urlShortner.exceptions.UrlNotFoundException;
import com.jb.urlShortner.urlShortner.repository.UrlShortenerRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AnalyticsService {

    private final UrlShortenerRepository repository;

    private final String appDns;

    public AnalyticsService(final UrlShortenerRepository repository,
                            @Value("${application.baseUrl}") final String appDns) {
        this.appDns = appDns;
        this.repository = repository;
    }

    public AnalyticsData getAnalytics(String shortUrl) {
        String urlCode = shortUrl.replaceFirst("^https?://", "");
        String hashValue = urlCode.split("/")[1];
        URLCollection urlData = repository.findOneByHashValue(hashValue)
                .orElseThrow(() -> new UrlNotFoundException(hashValue));
        return new AnalyticsData(
                String.format(appDns + "%s", urlData.getHashValue()),
                Long.parseLong(String.valueOf(urlData.getClickCount())),
                urlData.getCreatedDate(),
                urlData.getExpirationDate()
        );
    }
}
