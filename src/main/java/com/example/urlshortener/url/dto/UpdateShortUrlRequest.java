package com.example.urlshortener.url.dto;

// Separate from Create so PUT rules can evolve later
// without breaking the POST contract.
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateShortUrlRequest(
    @Schema(description = "New destination URL", example = "https://www.example.com/updated")
    @NotBlank(message = "url is required")
    @Size(max = 2048, message = "url is too long")
    String url
) {
}
