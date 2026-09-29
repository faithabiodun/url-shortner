package com.example.urlshortener.exception;

// reason: I throw this when my URL lacks http(s)://, so my handler returns 400
public class InvalidUrlException extends RuntimeException {
  public InvalidUrlException(String message) {
    super(message);
  }
}
