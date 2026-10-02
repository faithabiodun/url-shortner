package com.example.urlshortener.url.dto;

// Returned on POST/GET/PUT, matches the API contract.
// Separate from the entity so the API shape stays controlled.
import java.time.Instant;

public record ShortUrlResponse(
    Long id,
    String url,
    String shortCode,
    Instant createdAt,
    Instant updatedAt
) {
}
