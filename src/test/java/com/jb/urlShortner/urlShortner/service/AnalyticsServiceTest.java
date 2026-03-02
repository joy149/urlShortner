package com.jb.urlShortner.urlShortner.service;

import com.jb.urlShortner.urlShortner.domain.AnalyticsData;
import com.jb.urlShortner.urlShortner.domain.AnalyticsRequest;
import com.jb.urlShortner.urlShortner.domain.URLCollection;
import com.jb.urlShortner.urlShortner.repository.UrlShortenerRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private UrlShortenerRepository repository;

    private AnalyticsService service;

    @BeforeEach
    void setUp() {
        service = new AnalyticsService(repository, "https://sho.rt/");
    }

    @Test
    void getMyUrlsMapsFieldsExpectedByFrontend() {
        URLCollection first = URLCollection.builder()
                .id(new ObjectId())
                .hashValue("abc1234")
                .resolvedUrl("https://example.com")
                .createdDate(LocalDateTime.now().minusDays(1))
                .expirationDate(LocalDateTime.now().plusDays(3))
                .clickCount(5)
                .build();
        URLCollection second = URLCollection.builder()
                .id(null)
                .hashValue("xyz9999")
                .resolvedUrl("https://another.com")
                .createdDate(LocalDateTime.now().minusDays(2))
                .expirationDate(LocalDateTime.now().minusHours(1))
                .clickCount(0)
                .build();

        when(repository.findAllByOwnerLoginOrderByCreatedDateDesc("alice"))
                .thenReturn(List.of(first, second));

        List<AnalyticsData> result = service.getMyUrls("alice");

        assertEquals(2, result.size());
        assertEquals(first.getId().toHexString(), result.get(0).getId());
        assertEquals("https://sho.rt/abc1234", result.get(0).getShortUrl());
        assertEquals("xyz9999", result.get(1).getId());
    }

    @Test
    void getMySummaryCalculatesTotalsActiveAndExpired() {
        URLCollection activeOne = URLCollection.builder()
                .id(new ObjectId())
                .hashValue("a1")
                .resolvedUrl("https://a.com")
                .createdDate(LocalDateTime.now().minusDays(1))
                .expirationDate(LocalDateTime.now().plusDays(1))
                .clickCount(10)
                .build();
        URLCollection activeTwoWithNullClicks = URLCollection.builder()
                .id(new ObjectId())
                .hashValue("a2")
                .resolvedUrl("https://b.com")
                .createdDate(LocalDateTime.now().minusDays(2))
                .expirationDate(LocalDateTime.now().plusHours(2))
                .clickCount(null)
                .build();
        URLCollection expired = URLCollection.builder()
                .id(new ObjectId())
                .hashValue("a3")
                .resolvedUrl("https://c.com")
                .createdDate(LocalDateTime.now().minusDays(3))
                .expirationDate(LocalDateTime.now().minusMinutes(1))
                .clickCount(2)
                .build();

        when(repository.findAllByOwnerLoginOrderByCreatedDateDesc("alice"))
                .thenReturn(List.of(activeOne, activeTwoWithNullClicks, expired));

        AnalyticsRequest summary = service.getMySummary("alice");

        assertEquals(3, summary.getTotalUrls());
        assertEquals(12, summary.getTotalClicks());
        assertEquals(2, summary.getActiveUrls());
        assertEquals(1, summary.getExpiredUrls());
    }
}
