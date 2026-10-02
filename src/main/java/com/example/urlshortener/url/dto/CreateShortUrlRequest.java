package com.example.urlshortener.url.dto;

// POST /shorten body shape. url is required, customCode + expiresAt are optional.
// Validation runs before the service, so the DB never stores junk.
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record CreateShortUrlRequest(
    @Schema(description = "Long URL", example = "https://spring.io/guides/gs/rest-service/")
    @NotBlank(message = "url is required")
    @Size(max = 2048, message = "url is too long")
    String url,

    @Schema(description = "Optional custom alias, 4-20 chars", example = "my-link1", nullable = true)
    @Size(min = 4, max = 20, message = "customCode must be 4-20 chars")
    @Pattern(regexp = "^[a-zA-Z0-9_-]*$", message = "customCode may contain letters, numbers, _ and -")
    String customCode,

    @Schema(description = "Optional expiry time, must be in the future", nullable = true)
    @Future(message = "expiresAt must be in the future")
    Instant expiresAt
) {
}
