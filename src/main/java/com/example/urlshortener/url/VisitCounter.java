package com.example.urlshortener.url;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Counts visits off the request thread. The redirect returns 302 immediately
// and this persists the +1 in the background, so a viral link never makes
// readers queue on its database row. Stats lag slightly by design.
// Separate bean because @Async only works when called through the proxy.
@Service
public class VisitCounter {

  private final ShortUrlRepository repo;

  public VisitCounter(ShortUrlRepository repo) {
    this.repo = repo;
  }

  // Single atomic UPDATE, no read-modify-write: concurrent visits can never
  // overwrite each other and lose counts.
  @Async("visitExecutor")
  @Transactional
  public void countAsync(String shortCode) {
    repo.incrementAccessCount(shortCode);
  }
}
