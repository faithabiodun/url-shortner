package com.example.urlshortener;

// @SpringBootApplication turns on my auto-config, web server, and JPA in one line,
// so I don't configure each piece by hand
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UrlShortenerApplication {
  // my entry point — I press Run on this in IntelliJ
  public static void main(String[] args) {
    SpringApplication.run(UrlShortenerApplication.class, args);
  }
}
