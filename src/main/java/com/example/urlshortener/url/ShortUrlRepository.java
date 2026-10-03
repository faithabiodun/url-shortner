package com.example.urlshortener.url;

// JpaRepository gives save/findById/findAll/delete for free,
// so only shortCode lookups are declared and Spring writes the SQL.
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {
  // SELECT ... WHERE short_code=? powers GET/PUT/DELETE/stats.
  Optional<ShortUrl> findByShortCode(String shortCode);

  // Checked in a loop before save so codes stay unique.
  boolean existsByShortCode(String shortCode);

  // Atomic +1 with no prior read: safe under concurrent visits, never loses counts.
  @Modifying
  @Query("UPDATE ShortUrl s SET s.accessCount = s.accessCount + 1 WHERE s.shortCode = :code")
  int incrementAccessCount(@Param("code") String shortCode);
}
