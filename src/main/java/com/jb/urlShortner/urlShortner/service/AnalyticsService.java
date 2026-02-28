package com.jb.urlShortner.urlShortner.service;

import com.jb.urlShortner.urlShortner.domain.AnalyticsData;
import com.jb.urlShortner.urlShortner.domain.AnalyticsRequest;
import com.jb.urlShortner.urlShortner.domain.URLCollection;
import com.jb.urlShortner.urlShortner.repository.UrlShortenerRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AnalyticsService {

    private final String appDns;
    private final UrlShortenerRepository urlShortenerRepository;

    public AnalyticsService(final UrlShortenerRepository urlShortenerRepository,
                            final @Value("${application.baseUrl}") String appDns) {
        this.urlShortenerRepository = urlShortenerRepository;
        this.appDns = appDns;
    }

    public List<AnalyticsData> getMyUrls(final String ownerLogin) {
        return urlShortenerRepository.findAllByOwnerLoginOrderByCreatedDateDesc(ownerLogin).stream()
                .map(this::toAnalyticsData)
                .toList();
    }

    public AnalyticsRequest getMySummary(final String ownerLogin) {
        final List<URLCollection> urls = urlShortenerRepository.findAllByOwnerLoginOrderByCreatedDateDesc(ownerLogin);
        final LocalDateTime now = LocalDateTime.now();

        int totalUrls = urls.size();
        int totalClicks = urls.stream().mapToInt(url -> url.getClickCount() == null ? 0 : url.getClickCount()).sum();
        int activeUrls = (int) urls.stream()
                .filter(url -> url.getExpirationDate() != null && url.getExpirationDate().isAfter(now))
                .count();
        int expiredUrls = totalUrls - activeUrls;

        return AnalyticsRequest.builder()
                .totalUrls(totalUrls)
                .totalClicks(totalClicks)
                .activeUrls(activeUrls)
                .expiredUrls(expiredUrls)
                .build();
    }

    private AnalyticsData toAnalyticsData(URLCollection url) {
        String id = url.getId() == null ? url.getHashValue() : url.getId().toHexString();
        return AnalyticsData.builder()
                .id(id)
                .longUrl(url.getResolvedUrl())
                .shortUrl(String.format(appDns.concat("%s"), url.getHashValue()))
                .createdAt(url.getCreatedDate())
                .expirationAt(url.getExpirationDate())
                .clickCount(url.getClickCount())
                .build();
    }
}
