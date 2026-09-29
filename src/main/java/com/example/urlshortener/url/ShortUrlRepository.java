package com.example.urlshortener.url;

// JpaRepository gives me save/findById/findAll/delete free,
// so I only declare my shortCode lookups and Spring writes the SQL
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {
  // reason: SELECT ... WHERE short_code=? powers my GET/PUT/DELETE/stats
  Optional<ShortUrl> findByShortCode(String shortCode);

  // reason: I check this in a loop before save so my codes stay unique
  boolean existsByShortCode(String shortCode);
}
