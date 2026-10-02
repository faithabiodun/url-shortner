package com.example.urlshortener.exception;

// 410 Gone: link existed but its expiresAt has passed.
public class ExpiredLinkException extends RuntimeException {
  public ExpiredLinkException(String message) {
    super(message);
  }
}
