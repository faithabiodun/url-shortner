package com.example.urlshortener.exception;

import java.time.Instant;

// shared shape for every error response, keeps frontend parsing simple
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path
) {
}
