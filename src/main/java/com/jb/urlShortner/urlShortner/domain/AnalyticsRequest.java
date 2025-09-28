package com.jb.urlShortner.urlShortner.domain;

import jakarta.validation.constraints.NotEmpty;

public record AnalyticsRequest(@NotEmpty String shortUrl) {
}
