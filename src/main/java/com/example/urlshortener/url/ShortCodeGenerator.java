package com.example.urlshortener.url;

// @Component lets the service inject this, keeping generation separate from rules.
import org.springframework.stereotype.Component;
import java.security.SecureRandom;

@Component
public class ShortCodeGenerator {

  // 62 chars keeps codes short yet huge in combos (6 chars = ~56 billion).
  private static final String CHARS =
      "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
  private final SecureRandom random = new SecureRandom();

  // SecureRandom (not Math.random) so codes are not guessable.
  public String generate(int length) {
    StringBuilder sb = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
    }
    return sb.toString();
  }
}
