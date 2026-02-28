package com.jb.urlShortner.urlShortner.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AnalyticsRequest {
    private final Integer totalUrls;
    private final Integer totalClicks;
    private final Integer activeUrls;
    private final Integer expiredUrls;
}
