package com.example.urlshortener.url;

// I test my service rules without Postgres: I mock only my repository interface
// (mocking classes breaks on very new JDKs), and I use my real generator
// or a tiny fake when I need fixed codes
import com.example.urlshortener.exception.InvalidUrlException;
import com.example.urlshortener.exception.ResourceNotFoundException;
import com.example.urlshortener.url.dto.ShortUrlResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import java.util.Queue;
import java.util.ArrayDeque;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShortUrlServiceTest {

  @Mock ShortUrlRepository repo;

  ShortUrlService service;
  ShortCodeGenerator realGenerator = new ShortCodeGenerator();

  @BeforeEach
  void setup() {
    // reason: real generator so I avoid mocking a concrete class
    service = new ShortUrlService(repo, realGenerator);
  }

  // reason: tiny fake that returns my fixed codes in order, so I prove my retry loop
  static class FixedGenerator extends ShortCodeGenerator {
    private final Queue<String> codes;
    FixedGenerator(String... values) { codes = new ArrayDeque<>(java.util.List.of(values)); }
    @Override public String generate(int length) { return codes.poll(); }
  }

  @Test
  void createGeneratesUniqueCodeAndSaves() {
    // reason: first code collides so I prove my do/while retry works
    service = new ShortUrlService(repo, new FixedGenerator("abc123", "xyz789"));
    when(repo.existsByShortCode("abc123")).thenReturn(true);
    when(repo.existsByShortCode("xyz789")).thenReturn(false);
    when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

    ShortUrlResponse out = service.create("https://example.com/long");

    assertThat(out.shortCode()).isEqualTo("xyz789");
    assertThat(out.url()).isEqualTo("https://example.com/long");
  }

  @Test
  void createRejectsBadUrl() {
    assertThatThrownBy(() -> service.create("not-a-url"))
        .isInstanceOf(InvalidUrlException.class);
  }

  @Test
  void getIncrementsAccessCountOnce() {
    ShortUrl saved = new ShortUrl("https://example.com/a", "abc123");
    when(repo.findByShortCode("abc123")).thenReturn(Optional.of(saved));

    service.get("abc123");

    assertThat(saved.getAccessCount()).isEqualTo(1);
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
