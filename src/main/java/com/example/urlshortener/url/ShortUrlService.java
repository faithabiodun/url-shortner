package com.example.urlshortener.url;

import java.time.Instant;

import com.example.urlshortener.exception.InvalidUrlException;
import com.example.urlshortener.exception.ResourceNotFoundException;
import com.example.urlshortener.url.dto.ShortUrlResponse;
import com.example.urlshortener.url.dto.ShortUrlStatsResponse;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// I put all my business logic here for creating, getting, updating and deleting urls
@Service
public class ShortUrlService {

    // I make all my codes 6 chars long
    private static final int SHORT_CODE_LENGTH = 6;

    // I use this to talk to my db
    private final ShortUrlRepository repository;

    // I use this to make random codes
    private final ShortCodeGenerator generator;

    // I inject my repo and generator here so I can test easily
    public ShortUrlService(ShortUrlRepository repository, ShortCodeGenerator generator) {
        this.repository = repository;
        this.generator = generator;
    }

    // I check that the url starts with http:// or https://
    // if not I throw my InvalidUrlException
    private void checkUrl(String url) {

        // I also check null here in case I call the service directly without validation
        if (url == null || !(url.startsWith("http://") || url.startsWith("https://"))) {
            throw new InvalidUrlException("url must start with http:// or https://");
        }
    }

    // I use this to create and save a new short url
    @Transactional
    public ShortUrlResponse create(String url) {

        checkUrl(url);

        // I keep generating until I find a code that is not taken
        String code;

        do {
            code = generator.generate(SHORT_CODE_LENGTH);
        } while (repository.existsByShortCode(code));

        // I save my new entity then convert it to a response
        ShortUrl saved = repository.save(new ShortUrl(url, code));

        return toResponse(saved);
    }

    // I use this to find a code and I also count the access
    @Transactional
    public ShortUrlResponse get(String code) {

        ShortUrl entity = findOrThrow(code);

        // I bump the counter, I dont need save() because @Transactional auto saves it
        entity.setAccessCount(entity.getAccessCount() + 1);

        return toResponse(entity);
    }

    // I use this to change the long url of an existing code
    @Transactional
    public ShortUrlResponse update(String code, String url) {

        checkUrl(url);

        ShortUrl entity = findOrThrow(code);

        entity.setUrl(url);
        entity.setUpdatedAt(Instant.now());

        return toResponse(entity);
    }

    // I use this to delete a short url by code
    @Transactional
    public void delete(String code) {

        ShortUrl entity = findOrThrow(code);

        repository.delete(entity);
    }

    // I use this to get stats including how many times it was accessed
    @Transactional(readOnly = true)
    public ShortUrlStatsResponse stats(String code) {

        ShortUrl entity = findOrThrow(code);

        return new ShortUrlStatsResponse(
                entity.getId(),
                entity.getUrl(),
                entity.getShortCode(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getAccessCount()
        );
    }

    // I use this helper so I dont repeat the same find or throw code
    private ShortUrl findOrThrow(String code) {
        return repository.findByShortCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("not found: " + code));
    }

    // I use this helper to turn my entity into the json I send back
    private ShortUrlResponse toResponse(ShortUrl entity) {
        return new ShortUrlResponse(
                entity.getId(),
                entity.getUrl(),
                entity.getShortCode(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
