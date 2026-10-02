package com.example.urlshortener.url;

// @Service holds all roadmap rules, keeping the controller thin
// and giving a single place to debug.
import com.example.urlshortener.exception.AliasTakenException;
import com.example.urlshortener.exception.ExpiredLinkException;
import com.example.urlshortener.exception.InvalidUrlException;
import com.example.urlshortener.exception.ResourceNotFoundException;
import com.example.urlshortener.url.dto.ShortUrlResponse;
import com.example.urlshortener.url.dto.ShortUrlStatsResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.net.URI;
import java.time.Instant;

@Service
public class ShortUrlService {

  private final ShortUrlRepository repo;
  private final ShortCodeGenerator generator;

  // Constructor injection keeps dependencies explicit and testable.
  public ShortUrlService(ShortUrlRepository repo, ShortCodeGenerator generator) {
    this.repo = repo;
    this.generator = generator;
  }

  // Proper URL check: parses with URI, requires http/https scheme + host.
  // startsWith alone would accept "https://" with nothing after it.
  private void checkUrl(String url) {
    if (url == null || url.isBlank() || url.length() > 2048) {
      throw new InvalidUrlException("url is required (max 2048)");
    }
    try {
      URI uri = new URI(url);
      String scheme = uri.getScheme();
      if (!("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))) {
        throw new InvalidUrlException("url must start with http:// or https://");
      }
      if (uri.getHost() == null) {
        throw new InvalidUrlException("url must contain a valid host");
      }
    } catch (java.net.URISyntaxException e) {
      throw new InvalidUrlException("malformed url: " + e.getMessage());
    }
  }

  // Custom alias rules live here (DTO also checks pattern, this checks DB).
  private void checkCustomCode(String customCode) {
    if (customCode == null || customCode.isBlank()) {
      return;
    }
    if (!customCode.matches("^[a-zA-Z0-9_-]{4,20}$")) {
      throw new InvalidUrlException("customCode must be 4-20 chars: letters, numbers, _ and -");
    }
    if (repo.existsByShortCode(customCode)) {
      throw new AliasTakenException("customCode already taken: " + customCode);
    }
  }

  // Shared expiry gate: expired links behave as gone (410), not found (404).
  private void checkExpired(ShortUrl found) {
    if (found.isExpired()) {
      throw new ExpiredLinkException("link expired: " + found.getShortCode());
    }
  }

  // Old signature kept for existing unit tests.
  @Transactional
  public ShortUrlResponse create(String url) {
    return create(url, null, null);
  }

  // Loop on existsByShortCode guarantees uniqueness even on random collision.
  // If customCode is given, use it (409 if taken), else generate 6 random chars.
  @Transactional
  public ShortUrlResponse create(String url, String customCode, Instant expiresAt) {
    checkUrl(url);
    if (expiresAt != null && expiresAt.isBefore(Instant.now())) {
      throw new InvalidUrlException("expiresAt must be in the future");
    }
    String code;
    if (customCode != null && !customCode.isBlank()) {
      checkCustomCode(customCode);
      code = customCode;
    } else {
      do {
        code = generator.generate(6);
      } while (repo.existsByShortCode(code));
    }
    return toJson(repo.save(new ShortUrl(url, code, expiresAt)));
  }

  // Increment ONLY on real reads, so stats count actual visits.
  @Transactional
  public ShortUrlResponse get(String shortCode) {
    ShortUrl found = repo.findByShortCode(shortCode)
        .orElseThrow(() -> new ResourceNotFoundException("not found: " + shortCode));
    checkExpired(found);
    found.setAccessCount(found.getAccessCount() + 1);
    return toJson(found);
  }

  // Bump updatedAt so clients can tell edit time from creation.
  @Transactional
  public ShortUrlResponse update(String shortCode, String url) {
    checkUrl(url);
    ShortUrl found = repo.findByShortCode(shortCode)
        .orElseThrow(() -> new ResourceNotFoundException("not found: " + shortCode));
    checkExpired(found);
    found.setUrl(url);
    found.setUpdatedAt(Instant.now());
    return toJson(found);
  }

  @Transactional
  public void delete(String shortCode) {
    ShortUrl found = repo.findByShortCode(shortCode)
        .orElseThrow(() -> new ResourceNotFoundException("not found: " + shortCode));
    repo.delete(found);
  }

  // readOnly + no increment so viewing stats never inflates the count.
  @Transactional(readOnly = true)
  public ShortUrlStatsResponse stats(String shortCode) {
    ShortUrl found = repo.findByShortCode(shortCode)
        .orElseThrow(() -> new ResourceNotFoundException("not found: " + shortCode));
    checkExpired(found);
    return new ShortUrlStatsResponse(
        found.getId(), found.getUrl(), found.getShortCode(),
        found.getCreatedAt(), found.getUpdatedAt(), found.getAccessCount());
  }

  // Convert entity to DTO in one place so the API shape stays consistent.
  private ShortUrlResponse toJson(ShortUrl saved) {
    return new ShortUrlResponse(
        saved.getId(), saved.getUrl(), saved.getShortCode(),
        saved.getCreatedAt(), saved.getUpdatedAt());
  }
}
