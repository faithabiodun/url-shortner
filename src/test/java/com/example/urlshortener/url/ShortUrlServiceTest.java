package com.example.urlshortener.url;

// Service rules without Postgres: only the repository interface is mocked.
// Everything else is real: generator, link resolver, background counter
// (its @Async is inert without Spring, so it runs inline) and a real
// in-memory cache manager. Mocking concrete classes breaks on very new JDKs.
import com.example.urlshortener.exception.InvalidUrlException;
import com.example.urlshortener.exception.ResourceNotFoundException;
import com.example.urlshortener.url.dto.ShortUrlResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import java.util.Optional;
import java.util.Queue;
import java.util.ArrayDeque;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShortUrlServiceTest {

  @Mock ShortUrlRepository repo;

  ShortUrlService service;
  ShortCodeGenerator realGenerator = new ShortCodeGenerator();

  private ShortUrlService build(ShortCodeGenerator generator) {
    return new ShortUrlService(repo, generator,
        new LinkResolver(repo), new VisitCounter(repo),
        new ConcurrentMapCacheManager("redirects"));
  }

  @BeforeEach
  void setup() {
    service = build(realGenerator);
  }

  // Tiny fake that returns fixed codes in order, proving the retry loop.
  static class FixedGenerator extends ShortCodeGenerator {
    private final Queue<String> codes;
    FixedGenerator(String... values) { codes = new ArrayDeque<>(java.util.List.of(values)); }
    @Override public String generate(int length) { return codes.poll(); }
  }

  @Test
  void createGeneratesUniqueCodeAndSaves() {
    // First code collides so the do/while retry is proven.
    ShortUrlService retrying = build(new FixedGenerator("abc123", "xyz789"));
    when(repo.existsByShortCode("abc123")).thenReturn(true);
    when(repo.existsByShortCode("xyz789")).thenReturn(false);
    when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

    ShortUrlResponse out = retrying.create("https://example.com/long");

    assertThat(out.shortCode()).isEqualTo("xyz789");
    assertThat(out.url()).isEqualTo("https://example.com/long");
  }

  @Test
  void createRejectsBadUrl() {
    assertThatThrownBy(() -> service.create("not-a-url"))
        .isInstanceOf(InvalidUrlException.class);
  }

  @Test
  void getReturnsUrlAndCountsInBackground() {
    ShortUrl saved = new ShortUrl("https://example.com/a", "abc123");
    when(repo.findByShortCode("abc123")).thenReturn(Optional.of(saved));

    ShortUrlResponse out = service.get("abc123");

    // The 302 data comes back immediately; counting is delegated off-thread
    // (inline here without Spring) so the response never waits on a write.
    assertThat(out.url()).isEqualTo("https://example.com/a");
    verify(repo).incrementAccessCount("abc123");
  }

  @Test
  void statsDoesNotIncrement() {
    ShortUrl saved = new ShortUrl("https://example.com/a", "abc123");
    saved.setAccessCount(5);
    when(repo.findByShortCode("abc123")).thenReturn(Optional.of(saved));

    var stats = service.stats("abc123");

    assertThat(stats.accessCount()).isEqualTo(5);
    assertThat(saved.getAccessCount()).isEqualTo(5);
  }

  @Test
  void missingCodeThrows404() {
    when(repo.findByShortCode("nope")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.get("nope"))
        .isInstanceOf(ResourceNotFoundException.class);
  }
}
