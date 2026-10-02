package com.example.urlshortener.url;

// JpaRepository gives save/findById/findAll/delete for free,
// so only shortCode lookups are declared and Spring writes the SQL.
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {
  // SELECT ... WHERE short_code=? powers GET/PUT/DELETE/stats.
  Optional<ShortUrl> findByShortCode(String shortCode);

  // Checked in a loop before save so codes stay unique.
  boolean existsByShortCode(String shortCode);
}
