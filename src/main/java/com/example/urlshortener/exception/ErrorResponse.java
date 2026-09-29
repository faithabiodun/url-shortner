package com.example.urlshortener.exception;

import java.time.Instant;

// I made this record so all my errors look the same in json
// it holds the time, status code, error name, message and path
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path
) {
}
