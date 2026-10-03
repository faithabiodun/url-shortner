package com.example.urlshortener.url;

// @Service holds all roadmap rules, keeping the controller thin
// and giving a single place to debug.
import com.example.urlshortener.exception.AliasTakenException;
import com.example.urlshortener.exception.ExpiredLinkException;
import com.example.urlshortener.exception.InvalidUrlException;
import com.example.urlshortener.exception.ResourceNotFoundException;
import com.example.urlshortener.url.LinkResolver.CachedLink;
import com.example.urlshortener.url.dto.ShortUrlResponse;
import com.example.urlshortener.url.dto.ShortUrlStatsResponse;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.net.URI;
import java.time.Instant;

@Service
public class ShortUrlService {

  private final ShortUrlRepository repo;
  private final ShortCodeGenerator generator;
  private final LinkResolver linkResolver;
  private final VisitCounter visitCounter;
  private final CacheManager cacheManager;

  // Constructor injection keeps dependencies explicit and testable.
  public ShortUrlService(ShortUrlRepository repo, ShortCodeGenerator generator,
      LinkResolver linkResolver, VisitCounter visitCounter, CacheManager cacheManager) {
    this.repo = repo;
    this.generator = generator;
    this.linkResolver = linkResolver;
    this.visitCounter = visitCounter;
    this.cacheManager = cacheManager;
  }

  private Cache cache() {
    return cacheManager.getCache("redirects");
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

  // Shared expiry gate for entities loaded straight from the DB.
  private void checkExpired(ShortUrl found) {
    if (found.isExpired()) {
      throw new ExpiredLinkException("link expired: " + found.getShortCode());
    }
  }

  // Same gate for cached snapshots, evicting the stale entry first so the
  // next read goes to the DB instead of serving the dead link again.
  private void checkExpired(CachedLink link, String shortCode) {
    if (link.expiresAt() != null && Instant.now().isAfter(link.expiresAt())) {
      cache().evict(shortCode);
      throw new ExpiredLinkException("link expired: " + shortCode);
    }
  }

  // Old signature kept for existing unit tests.
  @Transactional
  public ShortUrlResponse create(String url) {
    return create(url, null, null);
  }

  // Loop on existsByShortCode guarantees uniqueness even on random collision.
  // If customCode is given, use it (409 if taken), else generate 6 random chars.
  // Warms the cache so the first redirect is already fast.
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
    ShortUrl saved = repo.save(new ShortUrl(url, code, expiresAt));
    cache().put(code, new CachedLink(
        saved.getId(), saved.getUrl(),
        saved.getCreatedAt(), saved.getUpdatedAt(), saved.getExpiresAt()));
    return toJson(saved);
  }

  // Hot path: served from cache when possible, counting fires in the
  // background so the 302 never waits on a database write.
  @Transactional(readOnly = true)
  public ShortUrlResponse get(String shortCode) {
    CachedLink link = linkResolver.resolve(shortCode);
    checkExpired(link, shortCode);
    visitCounter.countAsync(shortCode);
    return new ShortUrlResponse(
        link.id(), link.url(), shortCode, link.createdAt(), link.updatedAt());
  }

  // Bump updatedAt so clients can tell edit time from creation.
  // Evicts the cache so the next read sees the new destination.
  @Transactional
  public ShortUrlResponse update(String shortCode, String url) {
    checkUrl(url);
    ShortUrl found = repo.findByShortCode(shortCode)
        .orElseThrow(() -> new ResourceNotFoundException("not found: " + shortCode));
    checkExpired(found);
    found.setUrl(url);
    found.setUpdatedAt(Instant.now());
    cache().evict(shortCode);
    return toJson(found);
  }

  @Transactional
  public void delete(String shortCode) {
    ShortUrl found = repo.findByShortCode(shortCode)
        .orElseThrow(() -> new ResourceNotFoundException("not found: " + shortCode));
    repo.delete(found);
    cache().evict(shortCode);
  }

  // readOnly + no increment so viewing stats never inflates the count.
  // Reads the DB directly: the one place that always shows the true number.
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
