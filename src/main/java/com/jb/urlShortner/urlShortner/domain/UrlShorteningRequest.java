package com.jb.urlShortner.urlShortner.domain;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder(toBuilder = true)
public class UrlShorteningRequest {

    @NotEmpty
    private final String longUrl;
}
