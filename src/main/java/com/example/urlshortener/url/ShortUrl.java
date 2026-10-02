package com.example.urlshortener.url;

// @Entity maps this class to the short_urls table in Postgres,
// so you work with objects and Hibernate writes the SQL.
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "short_urls")
public class ShortUrl {

  // @Id is the primary key, IDENTITY lets the DB auto-increment it.
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // Original long URL, TEXT because URLs can be very long.
  @Column(nullable = false, columnDefinition = "TEXT")
  private String url;

  // Short abc123 code, unique so lookups are never ambiguous.
  @Column(name = "short_code", nullable = false, unique = true, length = 10)
  private String shortCode;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  // Starts at 0 and only GET increments it, so stats stay honest.
  @Column(name = "access_count", nullable = false)
  private long accessCount = 0;

  // JPA needs an empty constructor to rebuild rows from the DB.
  protected ShortUrl() {
  }

  // Stamp both times at creation so createdAt/updatedAt match the API contract.
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
