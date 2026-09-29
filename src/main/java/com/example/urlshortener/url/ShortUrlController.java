package com.example.urlshortener.url;

import com.example.urlshortener.url.dto.CreateShortUrlRequest;
import com.example.urlshortener.url.dto.ShortUrlResponse;
import com.example.urlshortener.url.dto.ShortUrlStatsResponse;
import com.example.urlshortener.url.dto.UpdateShortUrlRequest;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// I made this controller for all my /shorten endpoints
// POST /shorten makes a code
// GET /shorten/{code} gets it
// PUT /shorten/{code} updates it
// DELETE /shorten/{code} deletes it
// GET /shorten/{code}/stats shows stats
@RestController
@RequestMapping("/shorten")
public class ShortUrlController {

    // I use my service here for all the real work
    private final ShortUrlService service;

    // I inject my service through the constructor
    public ShortUrlController(ShortUrlService service) {
        this.service = service;
    }

    // I handle creating a new short url, I return 201 when it works
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ShortUrlResponse create(@Valid @RequestBody CreateShortUrlRequest body) {
        return service.create(body.url());
    }

    // I handle getting a url by its code
    @GetMapping("/{shortCode}")
    public ShortUrlResponse get(@PathVariable String shortCode) {
        return service.get(shortCode);
    }

    // I handle updating the long url of a code
    @PutMapping("/{shortCode}")
    public ShortUrlResponse update(@PathVariable String shortCode,
                                   @Valid @RequestBody UpdateShortUrlRequest body) {
        return service.update(shortCode, body.url());
    }

    // I handle deleting a code, I return 204 with no body
    @DeleteMapping("/{shortCode}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String shortCode) {
        service.delete(shortCode);
    }

    // I handle getting the stats of a code
    @GetMapping("/{shortCode}/stats")
    public ShortUrlStatsResponse stats(@PathVariable String shortCode) {
        return service.stats(shortCode);
    }
}
