package com.example.urlshortener.url.dto;

import java.time.Instant;

// I use this as what I send back for create, get and update
// it has the id, urls, codes and timestamps
public record ShortUrlResponse(
        Long id,
        String url,
        String shortCode,
        Instant createdAt,
        Instant updatedAt
) {
}
