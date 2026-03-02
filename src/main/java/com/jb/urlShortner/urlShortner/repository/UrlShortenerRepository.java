package com.jb.urlShortner.urlShortner.repository;

import com.jb.urlShortner.urlShortner.domain.URLCollection;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UrlShortenerRepository extends MongoRepository<URLCollection, String> {
    Optional<URLCollection> findOneByHashValue(String hashValue);

    Optional<URLCollection> findOneByResolvedUrl(String resolvedUrl);

    @Query("{ 'resolvedUrl': ?0, 'ownerLogin': ?1, 'expirationDate': { $gt: ?2} }")
    Optional<URLCollection> findOneByActiveResolvedUrlAndOwner(String resolvedUrl, String ownerLogin, LocalDateTime now);

    @Query("{ 'resolvedUrl': ?0, '$or': [ { 'ownerLogin': null }, { 'ownerLogin': { $exists: false } } ], 'expirationDate': { $gt: ?1} }")
    Optional<URLCollection> findOneByActiveResolvedUrlForAnonymous(String resolvedUrl, LocalDateTime now);

    @Query("{ 'hashValue': ?0, 'expirationDate': { $gt: ?1} }")
    Optional<URLCollection> findOneByActiveHashValue(String hashId, LocalDateTime now);

    List<URLCollection> findAllByOwnerLoginOrderByCreatedDateDesc(String ownerLogin);
}
