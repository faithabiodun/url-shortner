package com.example.urlshortener.url;

import com.example.urlshortener.url.dto.ShortUrlResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RedirectController {
    private final ShortUrlService service;
    public RedirectController(ShortUrlService service) {
        this.service = service;
    }

    //GET /abc123 -> 302 to original URL
    @GetMapping("/{shortCode:[a-zA-Z0-9]{6}}")
    public ResponseEntity<Object> redirect(@PathVariable String shortCode) {
        ShortUrlResponse found  = service.get(shortCode);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", found.url())
                .build();
    }
}
