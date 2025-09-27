package com.jb.urlShortner.urlShortner.domain;


import lombok.Builder;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "urlCollection")
@Builder(toBuilder = true)
public class URLCollection {

    private final String hashValue;
    private final String resolvedUrl;
    private final LocalDateTime createdDate;
    private final LocalDateTime expirationDate;
    private final Integer clickCount;

    public URLCollection(String hashValue,
                         String resolvedUrl,
                         LocalDateTime createdDate,
                         LocalDateTime expirationDate,
                         Integer clickCount) {
        this.hashValue = hashValue;
        this.resolvedUrl = resolvedUrl;
        this.createdDate = createdDate;
        this.expirationDate = expirationDate;
        this.clickCount = clickCount;
    }

    public String getHashValue() {
        return hashValue;
    }

    public String getResolvedUrl() {
        return resolvedUrl;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    public Integer getClickCount() {
        return clickCount;
    }

    @Override
    public String toString() {
        return "URLCollection{" +
                "hashValue='" + hashValue + '\'' +
                ", resolvedUrl='" + resolvedUrl + '\'' +
                ", createdDate=" + createdDate +
                ", expirationDate=" + expirationDate +
                ", clickCount=" + clickCount +
                '}';
    }
}
