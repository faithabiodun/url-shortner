package com.example.urlshortener.url.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// I use this as the body for POST /shorten
// it just holds the long url I want to shorten
public record CreateShortUrlRequest(

        // I check that url is not blank and max 2048 chars (normal browser limit)
        @NotBlank(message = "url must not be blank")
        @Size(max = 2048, message = "url must be at most 2048 characters")
        String url

) {
}
