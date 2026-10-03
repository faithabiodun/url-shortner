package com.example.urlshortener.url;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Full-stack test: real HTTP layer + real DB (H2 in tests) + real service.
// Unit tests mock the repo, this proves wiring works end to end.
@SpringBootTest
@AutoConfigureMockMvc
class ShortUrlApiTest {

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper json;
  @Autowired ShortUrlRepository repo;

  private String create(String body) throws Exception {
    String res = mvc.perform(post("/shorten")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();
    JsonNode node = json.readTree(res);
    return node.get("shortCode").asText();
  }

  // Counting is async: poll stats until the background +1 lands (max ~3s).
  private long awaitVisits(String code) throws Exception {
    for (int i = 0; i < 60; i++) {
      String stats = mvc.perform(get("/shorten/" + code + "/stats"))
          .andExpect(status().isOk())
          .andReturn().getResponse().getContentAsString();
      long n = json.readTree(stats).get("accessCount").asLong();
      if (n >= 1) {
        return n;
      }
      Thread.sleep(50);
    }
    return -1;
  }

  @Test
  void createRedirectAndStats() throws Exception {
    String code = create("{\"url\":\"https://www.example.com/\"}");

    // Public redirect: 302 + Location, counting happens in the background
    mvc.perform(get("/" + code))
        .andExpect(status().isFound())
        .andExpect(header().string("Location", "https://www.example.com/"));

    // Stats eventually reflects the visit; viewing stats adds no more
    assertThat(awaitVisits(code)).isEqualTo(1);
  }

  @Test
  void redirectIsCached() throws Exception {
    String code = create("{\"url\":\"https://example.com/before\"}");
    mvc.perform(get("/" + code)).andExpect(status().isFound());

    // Change the DB row behind the cache's back (no eviction involved).
    ShortUrl row = repo.findByShortCode(code).orElseThrow();
    row.setUrl("https://example.com/changed-directly");
    repo.save(row);

    // Still serves the cached destination: this repeat visit did no DB read.
    mvc.perform(get("/" + code))
        .andExpect(status().isFound())
        .andExpect(header().string("Location", "https://example.com/before"));
  }

  @Test
  void updateEvictsCache() throws Exception {
    String code = create("{\"url\":\"https://example.com/old\"}");
    mvc.perform(get("/" + code)).andExpect(status().isFound());

    mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/shorten/" + code)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"url\":\"https://example.com/new\"}"))
        .andExpect(status().isOk());

    // PUT evicted the entry, so the redirect serves the fresh destination.
    mvc.perform(get("/" + code))
        .andExpect(status().isFound())
        .andExpect(header().string("Location", "https://example.com/new"));
  }

  @Test
  void customAliasConflictIs409() throws Exception {
    create("{\"url\":\"https://example.com/a\",\"customCode\":\"my-link1\"}");

    mvc.perform(post("/shorten")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"url\":\"https://example.com/b\",\"customCode\":\"my-link1\"}"))
        .andExpect(status().isConflict());
  }

  @Test
  void badUrlIs400() throws Exception {
    mvc.perform(post("/shorten")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"url\":\"not-a-url\"}"))
        .andExpect(status().isBadRequest());

    // Passes startsWith but has no host, rejected by URI check
    mvc.perform(post("/shorten")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"url\":\"https://\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void expiredLinkIs410() throws Exception {
    ShortUrl saved = repo.save(new ShortUrl(
        "https://example.com/old", "old123",
        Instant.now().minus(1, ChronoUnit.HOURS)));

    mvc.perform(get("/shorten/" + saved.getShortCode()))
        .andExpect(status().isGone());

    mvc.perform(get("/shorten/" + saved.getShortCode() + "/stats"))
        .andExpect(status().isGone());
  }

  @Test
  void missingCodeIs404AndDeleteWorks() throws Exception {
    mvc.perform(get("/shorten/zzzzzz"))
        .andExpect(status().isNotFound());

    String code = create("{\"url\":\"https://example.com/del\"}");
    mvc.perform(delete("/shorten/" + code))
        .andExpect(status().isNoContent());
    mvc.perform(get("/shorten/" + code))
        .andExpect(status().isNotFound());
  }

  @Test
  void actuatorHealthIsUp() throws Exception {
    mvc.perform(get("/actuator/health"))
        .andExpect(status().isOk());
  }
}
