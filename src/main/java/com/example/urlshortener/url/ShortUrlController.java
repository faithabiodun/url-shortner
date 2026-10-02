package com.example.urlshortener.url;

// @RestController returns JSON, @RequestMapping sets base path /shorten.
// @Valid runs DTO checks first so bad input never reaches the service.
import com.example.urlshortener.url.dto.CreateShortUrlRequest;
import com.example.urlshortener.url.dto.ShortUrlResponse;
import com.example.urlshortener.url.dto.ShortUrlStatsResponse;
import com.example.urlshortener.url.dto.UpdateShortUrlRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Short URLs", description = "Create and manage short codes")
public class ShortUrlController {

  private final ShortUrlService service;

  public ShortUrlController(ShortUrlService service) {
    this.service = service;
  }

  // 201 CREATED signals a new row was made, per roadmap spec.
  @Operation(summary = "Create short URL")
  @ApiResponse(responseCode = "201", description = "Created")
  @ApiResponse(responseCode = "400", description = "Bad URL")
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ShortUrlResponse create(@Valid @RequestBody CreateShortUrlRequest body) {
    return service.create(body.url());
  }

  // GET returns the URL and counts a visit.
  @Operation(summary = "Get original URL as JSON (counts +1)")
  @ApiResponse(responseCode = "200", description = "Found")
  @ApiResponse(responseCode = "404", description = "Code not found")
  @GetMapping("/{shortCode}")
  public ShortUrlResponse get(
      @Parameter(description = "6-char code, e.g. aB3x9Z") @PathVariable String shortCode) {
    return service.get(shortCode);
  }

  @Operation(summary = "Update destination URL")
  @ApiResponse(responseCode = "200", description = "Updated")
  @ApiResponse(responseCode = "400", description = "Bad URL")
  @ApiResponse(responseCode = "404", description = "Code not found")
  @PutMapping("/{shortCode}")
  public ShortUrlResponse update(
      @Parameter(description = "6-char code, e.g. aB3x9Z") @PathVariable String shortCode,
      @Valid @RequestBody UpdateShortUrlRequest body) {
    return service.update(shortCode, body.url());
  }

  // 204 NO_CONTENT with empty body is the REST convention for successful delete.
  @Operation(summary = "Delete short URL")
  @ApiResponse(responseCode = "204", description = "Deleted")
  @ApiResponse(responseCode = "404", description = "Code not found")
  @DeleteMapping("/{shortCode}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(
      @Parameter(description = "6-char code, e.g. aB3x9Z") @PathVariable String shortCode) {
    service.delete(shortCode);
  }

  // Separate /stats path keeps counting reads apart from viewing counts.
  @Operation(summary = "Get visit stats (no increment)")
  @ApiResponse(responseCode = "200", description = "Found")
  @ApiResponse(responseCode = "404", description = "Code not found")
  @GetMapping("/{shortCode}/stats")
  public ShortUrlStatsResponse stats(
      @Parameter(description = "6-char code, e.g. aB3x9Z") @PathVariable String shortCode) {
    return service.stats(shortCode);
  }
}
