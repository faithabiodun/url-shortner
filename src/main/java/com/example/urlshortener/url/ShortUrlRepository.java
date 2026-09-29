package com.example.urlshortener.url;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// I made this repo for my ShortUrl entity
// I extend JpaRepository so spring gives me save, delete, find methods for free
@Repository
public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {

    // I use this to find one row by its short code
    Optional<ShortUrl> findByShortCode(String shortCode);

    // I use this to check if a code is already taken before I save
    boolean existsByShortCode(String shortCode);
}
