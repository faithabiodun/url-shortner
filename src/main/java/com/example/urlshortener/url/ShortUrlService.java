package com.example.urlshortener.url;

// @Service holds all 5 roadmap rules, keeping the controller thin
// and giving a single place to debug.
import com.example.urlshortener.exception.InvalidUrlException;
import com.example.urlshortener.exception.ResourceNotFoundException;
import com.example.urlshortener.url.dto.ShortUrlResponse;
import com.example.urlshortener.url.dto.ShortUrlStatsResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

  // Reject junk before touching the DB to keep the table clean.
  private void checkUrl(String url) {
    if (!(url.startsWith("http://") || url.startsWith("https://"))) {
      throw new InvalidUrlException("url must start with http:// or https://");
    }
  }

  // Loop on existsByShortCode guarantees uniqueness even on random collision.
  @Transactional
  public ShortUrlResponse create(String url) {
    checkUrl(url);
    String code;
    do {
      code = generator.generate(6);
    } while (repo.existsByShortCode(code));
    return toJson(repo.save(new ShortUrl(url, code)));
  }

  // Increment ONLY on real reads, so stats count actual visits.
  @Transactional
  public ShortUrlResponse get(String shortCode) {
    ShortUrl found = repo.findByShortCode(shortCode)
        .orElseThrow(() -> new ResourceNotFoundException("not found: " + shortCode));
    found.setAccessCount(found.getAccessCount() + 1);
    return toJson(found);
  }

  // Bump updatedAt so clients can tell edit time from creation.
  @Transactional
  public ShortUrlResponse update(String shortCode, String url) {
    checkUrl(url);
    ShortUrl found = repo.findByShortCode(shortCode)
        .orElseThrow(() -> new ResourceNotFoundException("not found: " + shortCode));
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
