package com.example.urlshortener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// this is my main app class
// the annotation turns on auto config, scanning and jpa repos for me
@SpringBootApplication
public class UrlShortenerApplication {

    // I start my app here, this boots tomcat and spring for me
    public static void main(String[] args) {
        SpringApplication.run(UrlShortenerApplication.class, args);
    }
}
