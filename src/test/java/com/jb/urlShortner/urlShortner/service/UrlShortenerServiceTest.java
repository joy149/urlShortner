package com.jb.urlShortner.urlShortner.service;

import com.jb.urlShortner.urlShortner.domain.URLCollection;
import com.jb.urlShortner.urlShortner.domain.UrlShorteningRequest;
import com.jb.urlShortner.urlShortner.exceptions.DuplicateAliasException;
import com.jb.urlShortner.urlShortner.exceptions.UrlExpirationException;
import com.jb.urlShortner.urlShortner.exceptions.UrlNotFoundException;
import com.jb.urlShortner.urlShortner.repository.UrlShortenerRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UrlShortenerServiceTest {

    @Mock
    private TinyUrlGenerator tinyUrlGenerator;
    @Mock
    private UrlShortenerRepository repository;
    @Mock
    private ObjectIdGenerator objectIdGenerator;

    private UrlShortenerService service;

    @BeforeEach
    void setUp() {
        service = new UrlShortenerService(tinyUrlGenerator, repository, objectIdGenerator, "https://sho.rt/");
    }

    @Test
    void getResolvedUrlValueThrowsWhenHashMissing() {
        when(repository.findOneByHashValue("missing")).thenReturn(Optional.empty());
        assertThrows(UrlNotFoundException.class, () -> service.getResolvedUrlValue("missing"));
    }

    @Test
    void getResolvedUrlValueThrowsWhenExpired() {
        URLCollection expired = url("abc1234", "https://example.com", "alice", "a@x.com",
                LocalDateTime.now().minusDays(2), LocalDateTime.now().minusHours(1), 4);
        when(repository.findOneByHashValue("abc1234")).thenReturn(Optional.of(expired));
        assertThrows(UrlExpirationException.class, () -> service.getResolvedUrlValue("abc1234"));
        verify(repository, never()).save(any());
    }

    @Test
    void getResolvedUrlValueReturnsAndIncrementsClicks() {
        URLCollection active = url("abc1234", "https://example.com", "alice", "a@x.com",
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1), 4);
        when(repository.findOneByHashValue("abc1234")).thenReturn(Optional.of(active));

        String resolved = service.getResolvedUrlValue("abc1234");

        assertEquals("https://example.com", resolved);
        ArgumentCaptor<URLCollection> saved = ArgumentCaptor.forClass(URLCollection.class);
        verify(repository).save(saved.capture());
        assertEquals(5, saved.getValue().getClickCount());
    }

    @Test
    void getShortenedUrlReturnsExistingForAuthenticatedOwner() {
        UrlShorteningRequest request = UrlShorteningRequest.builder().longUrl("https://example.com").build();
        URLCollection existing = url("hash111", "https://example.com", "alice", "a@x.com",
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(2), 1);
        when(repository.findOneByActiveResolvedUrlAndOwner(anyString(), anyString(), any())).thenReturn(Optional.of(existing));

        URLCollection response = service.getShortenedUrl(request, "alice", "a@x.com");

        assertEquals("https://sho.rt/hash111", response.getHashValue());
        verify(repository, never()).save(any());
    }

    @Test
    void getShortenedUrlReturnsExistingForAnonymous() {
        UrlShorteningRequest request = UrlShorteningRequest.builder().longUrl("https://example.com").build();
        URLCollection existing = url("anon111", "https://example.com", null, null,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(2), 2);
        when(repository.findOneByActiveResolvedUrlForAnonymous(anyString(), any())).thenReturn(Optional.of(existing));

        URLCollection response = service.getShortenedUrl(request, null, null);

        assertEquals("https://sho.rt/anon111", response.getHashValue());
        verify(repository, never()).save(any());
    }

    @Test
    void getShortenedUrlCreatesNewRecordWhenNoExistingAndNoCollision() {
        UrlShorteningRequest request = UrlShorteningRequest.builder()
                .longUrl("https://new.com")
                .expirationInDays(10)
                .build();
        ObjectId id = new ObjectId();
        when(objectIdGenerator.getObjectId()).thenReturn(id);
        when(tinyUrlGenerator.generateRandomHash()).thenReturn("newhash1");
        when(repository.findOneByActiveResolvedUrlForAnonymous(anyString(), any())).thenReturn(Optional.empty());
        when(repository.findOneByHashValue("newhash1")).thenReturn(Optional.empty());

        URLCollection response = service.getShortenedUrl(request, null, null);

        assertEquals("https://sho.rt/newhash1", response.getHashValue());
        ArgumentCaptor<URLCollection> saved = ArgumentCaptor.forClass(URLCollection.class);
        verify(repository).save(saved.capture());
        assertEquals(id, saved.getValue().getId());
        assertEquals("https://new.com", saved.getValue().getResolvedUrl());
        assertTrue(saved.getValue().getExpirationDate().isAfter(LocalDateTime.now().plusDays(9)));
    }

    @Test
    void getShortenedUrlReusesExpiredCollisionHash() {
        UrlShorteningRequest request = UrlShorteningRequest.builder().longUrl("https://new.com").build();
        ObjectId id = new ObjectId();
        when(objectIdGenerator.getObjectId()).thenReturn(id);
        when(tinyUrlGenerator.generateRandomHash()).thenReturn("oldhash1");
        when(repository.findOneByActiveResolvedUrlAndOwner(anyString(), anyString(), any())).thenReturn(Optional.empty());
        URLCollection expiredHash = url("oldhash1", "https://old.com", "bob", "b@x.com",
                LocalDateTime.now().minusDays(20), LocalDateTime.now().minusDays(1), 9);
        when(repository.findOneByHashValue("oldhash1")).thenReturn(Optional.of(expiredHash));

        URLCollection response = service.getShortenedUrl(request, "alice", "a@x.com");

        assertEquals("https://sho.rt/oldhash1", response.getHashValue());
        ArgumentCaptor<URLCollection> saved = ArgumentCaptor.forClass(URLCollection.class);
        verify(repository).save(saved.capture());
        assertEquals("https://new.com", saved.getValue().getResolvedUrl());
        assertEquals("alice", saved.getValue().getOwnerLogin());
        assertEquals(0, saved.getValue().getClickCount());
    }

    @Test
    void getShortenedUrlThrowsWhenGeneratedHashAlreadyActive() {
        UrlShorteningRequest request = UrlShorteningRequest.builder().longUrl("https://new.com").build();
        ObjectId id = new ObjectId();
        when(objectIdGenerator.getObjectId()).thenReturn(id);
        when(tinyUrlGenerator.generateRandomHash()).thenReturn("dupHash1");
        when(repository.findOneByActiveResolvedUrlForAnonymous(anyString(), any())).thenReturn(Optional.empty());
        URLCollection activeHash = url("dupHash1", "https://active.com", "bob", "b@x.com",
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(2), 7);
        when(repository.findOneByHashValue("dupHash1")).thenReturn(Optional.of(activeHash));

        assertThrows(DuplicateAliasException.class, () -> service.getShortenedUrl(request, null, null));
        verify(repository, never()).save(any());
    }

    private URLCollection url(String hash, String resolvedUrl, String ownerLogin, String ownerEmail,
                              LocalDateTime createdDate, LocalDateTime expirationDate, int clickCount) {
        return URLCollection.builder()
                .id(new ObjectId())
                .hashValue(hash)
                .resolvedUrl(resolvedUrl)
                .ownerLogin(ownerLogin)
                .ownerEmail(ownerEmail)
                .createdDate(createdDate)
                .expirationDate(expirationDate)
                .clickCount(clickCount)
                .build();
    }
}
