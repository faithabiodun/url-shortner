package com.example.urlshortener.url;

// @RestController = my methods return JSON; @RequestMapping = my base path /shorten;
// @Valid runs my DTO checks first so bad input never reaches my service
import com.example.urlshortener.url.dto.CreateShortUrlRequest;
import com.example.urlshortener.url.dto.ShortUrlResponse;
import com.example.urlshortener.url.dto.ShortUrlStatsResponse;
import com.example.urlshortener.url.dto.UpdateShortUrlRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/shorten")
public class ShortUrlController {

  private final ShortUrlService service;

  public ShortUrlController(ShortUrlService service) {
    this.service = service;
  }

  // reason: 201 CREATED signals I made a new row, per roadmap spec
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ShortUrlResponse create(@Valid @RequestBody CreateShortUrlRequest body) {
    return service.create(body.url());
  }

  // reason: this GET both returns my URL and counts a visit
  @GetMapping("/{shortCode}")
  public ShortUrlResponse get(@PathVariable String shortCode) {
    return service.get(shortCode);
  }

  @PutMapping("/{shortCode}")
  public ShortUrlResponse update(
      @PathVariable String shortCode,
      @Valid @RequestBody UpdateShortUrlRequest body) {
    return service.update(shortCode, body.url());
  }

  // reason: 204 NO_CONTENT with empty body is the REST convention for successful delete
  @DeleteMapping("/{shortCode}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable String shortCode) {
    service.delete(shortCode);
  }

  // reason: separate /stats path keeps counting reads apart from viewing counts
  @GetMapping("/{shortCode}/stats")
  public ShortUrlStatsResponse stats(@PathVariable String shortCode) {
    return service.stats(shortCode);
  }
}
