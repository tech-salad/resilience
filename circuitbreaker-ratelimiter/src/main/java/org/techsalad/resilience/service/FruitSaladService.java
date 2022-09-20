package org.techsalad.resilience.service;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.stereotype.Service;

@Service
public class FruitSaladService implements SaladService {
    @RateLimiter(name = "fruitRateLimiter")
    @Override
    public String saladType() {
        return "Grape salad!";
    }

    @RateLimiter(name = "fruitRateLimiter", fallbackMethod = "dressingFallback")
    @Override
    public String dressingType() {
        System.out.println("Maple sirup!");
        return "Maple sirup!";
    }

    @RateLimiter(name = "fruitRateLimiter")
    @Override
    public String summarizeOrder() {
        return "Grape salad with maple sirup!";
    }

    public String dressingFallback(Exception ex) {
        return "Fallback: maple sirup!";
    }
}
