package com.example.urlshortener.url.dto;

// separate from Create because I may allow different PUT rules later
// without breaking my POST contract
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateShortUrlRequest(
    @NotBlank(message = "url is required")
    @Size(max = 2048, message = "url is too long")
    String url
) {
}
