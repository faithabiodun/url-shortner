package com.example.urlshortener.url.dto;

// record = my POST /shorten body shape; @NotBlank stops empty URLs
// before they reach my service, so my DB never stores junk
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateShortUrlRequest(
    @NotBlank(message = "url is required")
    @Size(max = 2048, message = "url is too long")
    String url
) {
}
