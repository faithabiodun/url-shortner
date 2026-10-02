package com.example.urlshortener.exception;

// Fixed error shape (time + status + message + path) so errors stay readable in Postman.
import java.time.Instant;

public record ApiError(
    Instant timestamp,
    int status,
    String message,
    String path
) {
}
