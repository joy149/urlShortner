package com.jb.urlShortner.urlShortner.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AnalyticsData {
    private final String id;
    private final String longUrl;
    private final String shortUrl;
    private final LocalDateTime createdAt;
    private final LocalDateTime expirationAt;
    private final Integer clickCount;
}
