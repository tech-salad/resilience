package org.techsalad.timeout.service;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import io.github.resilience4j.timelimiter.event.TimeLimiterOnErrorEvent;
import io.github.resilience4j.timelimiter.event.TimeLimiterOnSuccessEvent;
import io.github.resilience4j.timelimiter.event.TimeLimiterOnTimeoutEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Service
@RequiredArgsConstructor
@Log
public class SaladsService {

  private static final String SALADS_TIME_LIMITER_NAME = "getSalads";

  private final TimeLimiterRegistry registry;

  @Value("${techsalad.waitDuration}")
  private Duration waitDuration;

  public CompletableFuture<List<String>> getSaladTypes() {
    return CompletableFuture.supplyAsync(() -> Arrays.asList("Ceasar", "Caprese"));
  }
  @TimeLimiter(name = SALADS_TIME_LIMITER_NAME)
  public CompletableFuture<List<String>> getAllSalads() {
    return CompletableFuture.supplyAsync(this::getSalads);
  }

  private List<String> getSalads() {
    waitMillis(waitDuration.toMillis());

    return Collections.singletonList("The best salad ever!");
  }

  private void waitMillis(long milliseconds) {
    try {
      Thread.sleep(milliseconds);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  @PostConstruct
  public void postConstruct() {
    this.registry.timeLimiter(SALADS_TIME_LIMITER_NAME).getEventPublisher().onTimeout(this::logTimeout);
    this.registry.timeLimiter(SALADS_TIME_LIMITER_NAME).getEventPublisher().onSuccess(this::logSuccess);
    this.registry.timeLimiter(SALADS_TIME_LIMITER_NAME).getEventPublisher().onError(this::logError);
  }

  private void logTimeout(final TimeLimiterOnTimeoutEvent event) {
    log.log(WARNING, "Getting the salads reached the timeout : {0}.", event.getCreationTime());
  }

  private void logSuccess(final TimeLimiterOnSuccessEvent event) {
    log.log(INFO, "Salads were retrieved successfully : {0}", event.getCreationTime());
  }

  private void logError(final TimeLimiterOnErrorEvent event) {
    log.log(INFO, "Salads were retrieved with error : {0}, {1}", new Object[]{event.getCreationTime(), event.getThrowable()});
  }
}
