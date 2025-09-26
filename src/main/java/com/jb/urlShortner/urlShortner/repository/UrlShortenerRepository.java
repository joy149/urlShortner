package com.jb.urlShortner.urlShortner.repository;

import com.jb.urlShortner.urlShortner.domain.URLCollection;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UrlShortenerRepository extends MongoRepository<URLCollection, String> {
    Optional<URLCollection> findOneByHashValue(String hashValue);
    Optional<URLCollection> findOneByResolvedUrl(String resolvedUrl);
}
