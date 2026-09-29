package com.example.urlshortener.url.dto;

import java.time.Instant;

// I use this for GET /shorten/{code}/stats
// its like my normal response but I also add accessCount
public record ShortUrlStatsResponse(
        Long id,
        String url,
        String shortCode,
        Instant createdAt,
        Instant updatedAt,
        long accessCount
) {
}
