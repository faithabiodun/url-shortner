package com.example.urlshortener.url.dto;

// what I return on POST/GET/PUT — matches roadmap JSON exactly,
// separate from my entity so I control my API shape
import java.time.Instant;

public record ShortUrlResponse(
    Long id,
    String url,
    String shortCode,
    Instant createdAt,
    Instant updatedAt
) {
}
