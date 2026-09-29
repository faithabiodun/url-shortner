package com.example.urlshortener.exception;

// @RestControllerAdvice catches my exceptions globally and returns JSON,
// so I never leak Spring's HTML whitelabel page to my API clients
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

  // reason: missing code = 404, client asked for something that isn't in my DB
  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ApiError> notFound(ResourceNotFoundException ex, HttpServletRequest req) {
    return ResponseEntity.status(404)
        .body(new ApiError(Instant.now(), 404, ex.getMessage(), req.getRequestURI()));
  }

  // reason: bad URL = 400, client must fix the request body
  @ExceptionHandler(InvalidUrlException.class)
  public ResponseEntity<ApiError> badUrl(InvalidUrlException ex, HttpServletRequest req) {
    return ResponseEntity.badRequest()
        .body(new ApiError(Instant.now(), 400, ex.getMessage(), req.getRequestURI()));
  }

  // reason: @Valid failures (blank/too-long) also = 400, caught separately by Spring
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
    return ResponseEntity.badRequest()
        .body(new ApiError(Instant.now(), 400, "validation failed: url is required (max 2048)", req.getRequestURI()));
  }
}
