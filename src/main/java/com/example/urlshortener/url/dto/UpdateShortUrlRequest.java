package com.example.urlshortener.url.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// I use this as the body for PUT /shorten/{shortCode}
// it holds the new long url I want to update to
public record UpdateShortUrlRequest(

        // I check that url is not blank and max 2048 chars
        @NotBlank(message = "url must not be blank")
        @Size(max = 2048, message = "url must be at most 2048 characters")
        String url

) {
}
