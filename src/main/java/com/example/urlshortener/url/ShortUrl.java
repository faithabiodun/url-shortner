package com.example.urlshortener.url;

// @Entity means this class IS my short_urls table in Postgres,
// so I work with objects and Hibernate writes my SQL
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "short_urls")
public class ShortUrl {

  // @Id = my primary key, IDENTITY = my DB auto-increments it so I never pick ids
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // my original long URL, TEXT because URLs can be very long
  @Column(nullable = false, columnDefinition = "TEXT")
  private String url;

  // my abc123 code, unique = DB rejects duplicates so my lookups never ambiguous
  @Column(name = "short_code", nullable = false, unique = true, length = 10)
  private String shortCode;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  // reason: starts at 0 and only my GET increments it, so my stats stay honest
  @Column(name = "access_count", nullable = false)
  private long accessCount = 0;

  // reason: JPA needs an empty constructor to rebuild my rows from the DB
  protected ShortUrl() {
  }

  // reason: I stamp both times at creation so my createdAt/updatedAt match roadmap JSON
  public ShortUrl(String url, String shortCode) {
    this.url = url;
    this.shortCode = shortCode;
    this.createdAt = Instant.now();
    this.updatedAt = Instant.now();
  }

  public Long getId() { return id; }
  public String getUrl() { return url; }
  public void setUrl(String url) { this.url = url; }
  public String getShortCode() { return shortCode; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
  public long getAccessCount() { return accessCount; }
  public void setAccessCount(long accessCount) { this.accessCount = accessCount; }
}
