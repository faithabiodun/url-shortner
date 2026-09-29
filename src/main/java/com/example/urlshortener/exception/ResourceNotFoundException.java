package com.example.urlshortener.exception;

// reason: I throw this when my shortCode lookup finds nothing, so my handler returns 404
public class ResourceNotFoundException extends RuntimeException {
  public ResourceNotFoundException(String message) {
    super(message);
  }
}
