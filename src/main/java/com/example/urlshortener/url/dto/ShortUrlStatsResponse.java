package com.example.urlshortener.url.dto;

// Same as ShortUrlResponse plus the counter. Only used on GET .../stats,
// so normal reads stay lean and stats stays explicit.
import java.time.Instant;

public record ShortUrlStatsResponse(
    Long id,
    String url,
    String shortCode,
    Instant createdAt,
    Instant updatedAt,
    long accessCount
) {
}
