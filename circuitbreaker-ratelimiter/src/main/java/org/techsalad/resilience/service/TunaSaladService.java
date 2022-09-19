package org.techsalad.resilience.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Service;

@Service
public class TunaSaladService implements SaladService {

    @CircuitBreaker(name = "tunaCircuitBreaker")
    @Override
    public String saladType() {
        return "Tuna Salad!";
    }

    @CircuitBreaker(name = "tunaCircuitBreaker", fallbackMethod = "drinkFallback")
    @Override
    public String dressingType() {
        throw new IllegalStateException("Cannot process dressing!");
    }

    @CircuitBreaker(name = "tunaCircuitBreaker")
    @Override
    public String summarizeOrder() {
        throw new IllegalStateException("Cannot process order!");
    }

    public String drinkFallback(Exception ex){
        return "Fallback: no dressing";
    }
}
