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

    // GET /abc123 -> 302 to original URL. Regex allows random 6-char codes
    // and custom aliases (4-20, no dots) so /swagger-ui.html still falls through.
    @Operation(summary = "Redirect to original URL (302, counts +1)")
    @ApiResponse(responseCode = "302", description = "Redirect to original URL")
    @ApiResponse(responseCode = "404", description = "Code not found")
    @ApiResponse(responseCode = "410", description = "Link expired")
    @GetMapping("/{shortCode:[a-zA-Z0-9_-]{4,20}}")
    public ResponseEntity<Void> redirect(
            @Parameter(description = "code or alias, e.g. aB3x9Z or my-link1") @PathVariable String shortCode) {
        ShortUrlResponse found = service.get(shortCode);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", found.url())
                .build();
    }
}
