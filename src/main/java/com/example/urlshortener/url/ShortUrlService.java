package com.example.urlshortener.url;

// @Service marks my brain: all 5 roadmap rules live here,
// so my controller stays thin and I know where to debug
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

  // reason: constructor injection makes my dependencies explicit and testable
  public ShortUrlService(ShortUrlRepository repo, ShortCodeGenerator generator) {
    this.repo = repo;
    this.generator = generator;
  }

  // reason: I reject junk before touching my DB so my table stays clean
  private void checkUrl(String url) {
    if (!(url.startsWith("http://") || url.startsWith("https://"))) {
      throw new InvalidUrlException("url must start with http:// or https://");
    }
  }

  // reason: loop on existsByShortCode guarantees uniqueness even on random collision
  @Transactional
  public ShortUrlResponse create(String url) {
    checkUrl(url);
    String code;
    do {
      code = generator.generate(6);
    } while (repo.existsByShortCode(code));
    return toJson(repo.save(new ShortUrl(url, code)));
  }

  // reason: I increment ONLY on real reads, so my stats count actual visits
  @Transactional
  public ShortUrlResponse get(String shortCode) {
    ShortUrl found = repo.findByShortCode(shortCode)
        .orElseThrow(() -> new ResourceNotFoundException("not found: " + shortCode));
    found.setAccessCount(found.getAccessCount() + 1);
    return toJson(found);
  }

  // reason: I bump updatedAt so clients can tell my edit time from creation
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

  // reason: readOnly + no increment so viewing stats never inflates my count
  @Transactional(readOnly = true)
  public ShortUrlStatsResponse stats(String shortCode) {
    ShortUrl found = repo.findByShortCode(shortCode)
        .orElseThrow(() -> new ResourceNotFoundException("not found: " + shortCode));
    return new ShortUrlStatsResponse(
        found.getId(), found.getUrl(), found.getShortCode(),
        found.getCreatedAt(), found.getUpdatedAt(), found.getAccessCount());
  }

  // reason: I convert entity→DTO in one place so my API shape stays consistent
  private ShortUrlResponse toJson(ShortUrl saved) {
    return new ShortUrlResponse(
        saved.getId(), saved.getUrl(), saved.getShortCode(),
        saved.getCreatedAt(), saved.getUpdatedAt());
  }
}
