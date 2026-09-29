package com.example.urlshortener.exception;

// reason: fixed error shape (time+status+message+path) so my Postman errors are always readable
import java.time.Instant;

public record ApiError(
    Instant timestamp,
    int status,
    String message,
    String path
) {
}
