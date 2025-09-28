package com.jb.urlShortner.urlShortner.domain;

import java.time.LocalDateTime;


public record AnalyticsData(String shortUrl, Long clickCount, LocalDateTime creationDate,
                            LocalDateTime linkExpirationDate) {
}
