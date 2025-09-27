package com.jb.urlShortner.urlShortner.domain;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Document(collection = "urlCollection")
@Builder(toBuilder = true)
public class URLCollection {

    @Id
    @JsonIgnore
    private final ObjectId id;
    private final String hashValue;
    private final String resolvedUrl;
    private final LocalDateTime createdDate;
    private final LocalDateTime expirationDate;
    private final Integer clickCount;

    public URLCollection(ObjectId id,
                         String hashValue,
                         String resolvedUrl,
                         LocalDateTime createdDate,
                         LocalDateTime expirationDate,
                         Integer clickCount) {
        this.id = id;
        this.hashValue = hashValue;
        this.resolvedUrl = resolvedUrl;
        this.createdDate = createdDate;
        this.expirationDate = expirationDate;
        this.clickCount = clickCount;
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
