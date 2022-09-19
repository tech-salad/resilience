package org.techsalad.resilience.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Log4j2
public class CeasarSaladService implements SaladService {

    @CircuitBreaker(name = "ceasarCircuitBreaker")
    @Override
    public String saladType() {
        return "Ceasar salad!";
    }

    @CircuitBreaker(name = "ceasarCircuitBreaker")
    @Override
    public String dressingType() {
        return  "Olive oil!";
    }

    @CircuitBreaker(name = "ceasarCircuitBreaker", fallbackMethod = "orderFallback")
    @Override
    public String summarizeOrder(){
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            log.error(e);
            throw new RuntimeException(e);
        }
        return "Ceasar salad with olive oil!";
    }

    public String orderFallback(Exception ex){
        return "Fallback: Ceasar salad with olive oil!";
    }
}
