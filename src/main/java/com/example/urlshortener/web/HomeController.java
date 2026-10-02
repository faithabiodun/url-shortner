package com.example.urlshortener.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// Serves the visual UI. The page itself is static/index.html
// (auto-copied into the jar). This controller gives "/" an explicit
// entry point so the UI works at http://localhost:8080/ next to the API.
@Controller
public class HomeController {

  @GetMapping("/")
  public String home() {
    return "forward:/index.html";
  }
}
