package com.example.urlshortener.url;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

// I made this entity for my short_urls table
// each row holds the long url, the short code, timestamps and access count
@Entity
@Table(name = "short_urls")
public class ShortUrl {

    // this is my primary key, postgres auto increments it
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // I store the original long url here, TEXT so it can be very long
    @Column(nullable = false, columnDefinition = "TEXT")
    private String url;

    // I store my unique short code here, max 10 chars like "aB3xYz"
    @Column(name = "short_code", nullable = false, unique = true, length = 10)
    private String shortCode;

    // I store when the row was created
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    // I store when the row was last updated
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // I count how many times the short code was used
    @Column(name = "access_count", nullable = false)
    private long accessCount = 0;

    // I need this empty constructor for JPA, I keep it protected
    // so my own code uses the other constructor below
    protected ShortUrl() {
    }

    // I use this to create a new short url with fresh timestamps
    public ShortUrl(String url, String code) {
        this.url = url;
        this.shortCode = code;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    // I return the db id
    public Long getId() {
        return id;
    }

    // I return the long url
    public String getUrl() {
        return url;
    }

    // I use this to change the long url
    public void setUrl(String url) {
        this.url = url;
    }

    // I return the short code
    public String getShortCode() {
        return shortCode;
    }

    // I return when it was created
    public Instant getCreatedAt() {
        return createdAt;
    }

    // I return when it was last updated
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    // I use this to update the updated_at time
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    // I return how many times it was accessed
    public long getAccessCount() {
        return accessCount;
    }

    // I use this to overwrite the counter
    public void setAccessCount(long accessCount) {
        this.accessCount = accessCount;
    }
}
