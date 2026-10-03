package com.example.urlshortener.url;

import java.time.Instant;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

// Fast read path for redirects. First visit loads from Postgres and the
// result is cached, repeat visits never touch the DB.
// Separate bean (not a method in ShortUrlService) because Spring's
// @Cacheable only works when called through the proxy — a self-call
// inside the same class would silently skip the cache.
@Service
public class LinkResolver {

  // Immutable snapshot: safe to share across threads from the cache.
  public record CachedLink(Long id, String url, Instant createdAt, Instant updatedAt, Instant expiresAt) {
  }

  private final ShortUrlRepository repo;

  public LinkResolver(ShortUrlRepository repo) {
    this.repo = repo;
  }

  // 404s are thrown, never cached — only successful lookups are stored.
  @Cacheable(value = "redirects", key = "#shortCode")
  public CachedLink resolve(String shortCode) {
    ShortUrl found = repo.findByShortCode(shortCode)
        .orElseThrow(() -> new com.example.urlshortener.exception.ResourceNotFoundException("not found: " + shortCode));
    return new CachedLink(
        found.getId(), found.getUrl(),
        found.getCreatedAt(), found.getUpdatedAt(), found.getExpiresAt());
  }
}
