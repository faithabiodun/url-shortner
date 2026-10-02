package com.example.urlshortener.exception;

// 409 Conflict: client asked for a custom alias that is already taken.
public class AliasTakenException extends RuntimeException {
  public AliasTakenException(String message) {
    super(message);
  }
}
