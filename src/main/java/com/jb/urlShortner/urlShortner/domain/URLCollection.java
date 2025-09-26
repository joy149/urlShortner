package com.jb.urlShortner.urlShortner.domain;


import lombok.Builder;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "urlCollection")
@Builder(toBuilder = true)
public class URLCollection {

    private final String hashValue;
    private final String resolvedUrl;

    public URLCollection(String hashValue, String resolvedUrl) {
        this.hashValue = hashValue;
        this.resolvedUrl = resolvedUrl;
    }

    public String getHashValue() {
        return hashValue;
    }

    public String getResolvedUrl() {
        return resolvedUrl;
    }

    @Override
    public String toString() {
        return "URLCollection{" +
                "hashValue='" + hashValue + '\'' +
                ", resolvedUrl='" + resolvedUrl + '\'' +
                '}';
    }
}
