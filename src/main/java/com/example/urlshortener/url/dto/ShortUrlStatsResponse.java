package com.example.urlshortener.url.dto;

// same as ShortUrlResponse plus my counter — only used on GET .../stats,
// so normal reads don't leak internals and stats stays explicit
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
