package com.example.urlshortener.url;

import com.example.urlshortener.url.dto.ShortUrlResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Redirect", description = "Public 302 redirect")
public class RedirectController {
    private final ShortUrlService service;
    public RedirectController(ShortUrlService service) {
        this.service = service;
    }

    // GET /abc123 -> 302 to original URL. Regex keeps only 6-char codes here
    // so /swagger-ui.html and other paths fall through.
    @Operation(summary = "Redirect to original URL (302, counts +1)")
    @ApiResponse(responseCode = "302", description = "Redirect to original URL")
    @ApiResponse(responseCode = "404", description = "Code not found")
    @GetMapping("/{shortCode:[a-zA-Z0-9]{6}}")
    public ResponseEntity<Void> redirect(
            @Parameter(description = "6-char code, e.g. aB3x9Z") @PathVariable String shortCode) {
        ShortUrlResponse found = service.get(shortCode);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", found.url())
                .build();
    }
}
