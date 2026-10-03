package com.example.urlshortener.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.concurrent.Executor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

// Infrastructure beans: what to cache, and where background work runs.
// The redirect path is 90%+ repeat visits, so caching it removes most DB reads.
@Configuration
public class CacheConfig {

  // Holds up to 10k links, each forgotten 10 min after write.
  // Bounded + TTL so memory can never grow forever and expired links fade out.
  @Bean
  public CacheManager cacheManager() {
    CaffeineCacheManager manager = new CaffeineCacheManager("redirects");
    manager.setCaffeine(Caffeine.newBuilder()
        .maximumSize(10_000)
        .expireAfterWrite(Duration.ofMinutes(10)));
    return manager;
  }

  // Background pool for visit counting. Small on purpose: counting must
  // never steal threads from request handling, a queue absorbs bursts.
  @Bean(name = "visitExecutor")
  public Executor visitExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(2);
    executor.setMaxPoolSize(4);
    executor.setQueueCapacity(500);
    executor.setThreadNamePrefix("visits-");
    executor.initialize();
    return executor;
  }
}
