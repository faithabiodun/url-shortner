package com.example.urlshortener.url.dto;

// POST /shorten body shape. @NotBlank stops empty URLs
// before they reach the service, so the DB never stores junk.
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateShortUrlRequest(
    @Schema(description = "Long URL", example = "https://spring.io/guides/gs/rest-service/")
    @NotBlank(message = "url is required")
    @Size(max = 2048, message = "url is too long")
    String url
) {
}
