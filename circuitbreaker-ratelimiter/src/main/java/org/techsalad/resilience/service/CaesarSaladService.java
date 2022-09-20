package org.techsalad.resilience.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Log4j2
public class CaesarSaladService implements SaladService {

    @CircuitBreaker(name = "caesarCircuitBreaker")
    @Override
    public String saladType() {
        return "Caesar salad!";
    }

    @CircuitBreaker(name = "caesarCircuitBreaker")
    @Override
    public String dressingType() {
        return  "Olive oil!";
    }

    @CircuitBreaker(name = "caesarCircuitBreaker", fallbackMethod = "orderFallback")
    @Override
    public String summarizeOrder(){
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            log.error(e);
            throw new RuntimeException(e);
        }
        return "Caesar salad with olive oil!";
    }

    public String orderFallback(Exception ex){
        return "Fallback: Caesar salad with olive oil!";
    }
}
