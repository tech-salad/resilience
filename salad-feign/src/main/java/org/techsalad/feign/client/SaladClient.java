package org.techsalad.feign.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "saladFeignClient", url = "${client.feign.url}")
public interface SaladClient {
    @GetMapping("/salad/{type}")
    @CircuitBreaker(name = "feignCircuitBreaker", fallbackMethod = "saladFallback")
    ResponseEntity<String> salad(@PathVariable(name = "type") final String salad);

    @GetMapping("/dressing/{type}")
    @CircuitBreaker(name = "feignCircuitBreaker", fallbackMethod = "dressingFallback")
    ResponseEntity<String> dressing(@PathVariable(name = "type") final String dressing);

    default ResponseEntity<String> dressingFallback(final String dressing, Throwable ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(dressing + " unavailable! No dressing for you!");
    }

    default ResponseEntity<String> saladFallback(final String salad, Throwable ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(salad + " not found! No salad for you!");
    }
}
