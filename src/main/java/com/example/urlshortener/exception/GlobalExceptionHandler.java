package com.example.urlshortener.exception;

// @RestControllerAdvice catches exceptions globally and returns JSON,
// so the API never leaks Spring's HTML whitelabel page.
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

  // Missing code = 404, client asked for something not in the DB.
  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ApiError> notFound(ResourceNotFoundException ex, HttpServletRequest req) {
    return ResponseEntity.status(404)
        .body(new ApiError(Instant.now(), 404, ex.getMessage(), req.getRequestURI()));
  }

  // Bad URL = 400, client must fix the request body.
  @ExceptionHandler(InvalidUrlException.class)
  public ResponseEntity<ApiError> badUrl(InvalidUrlException ex, HttpServletRequest req) {
    return ResponseEntity.badRequest()
        .body(new ApiError(Instant.now(), 400, ex.getMessage(), req.getRequestURI()));
  }

  // @Valid failures (blank/too-long) are also 400, caught separately by Spring.
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
    return ResponseEntity.badRequest()
        .body(new ApiError(Instant.now(), 400, "validation failed: url is required (max 2048)", req.getRequestURI()));
  }
}
