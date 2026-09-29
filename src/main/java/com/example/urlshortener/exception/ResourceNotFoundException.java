package com.example.urlshortener.exception;

// I made this exception for when a short code is not found in the db
// it will return a 404 through my global handler
public class ResourceNotFoundException extends RuntimeException {

    // I use this when I just want to pass a message like "not found: abc123"
    public ResourceNotFoundException(String message) {
        super(message);
    }

    // I use this when I also want to keep the original cause of the error
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
