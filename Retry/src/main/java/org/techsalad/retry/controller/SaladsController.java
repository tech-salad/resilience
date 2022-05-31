package org.techsalad.retry.controller;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.techsalad.retry.service.SaladsService;

import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.retry.event.RetryOnErrorEvent;
import io.github.resilience4j.retry.event.RetryOnRetryEvent;
import io.github.resilience4j.retry.event.RetryOnSuccessEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@RequiredArgsConstructor
@RestController
@Log
public class SaladsController {

  private static final String SALADS_RETRY_NAME = "getSalads";

  private final RetryRegistry registry;
  private final SaladsService saladsService;

  @Retry(name = SALADS_RETRY_NAME)
  @GetMapping("/salads")
  public List<String> getSalads() {
    return saladsService.getAllSalads();
  }

  @PostConstruct
  public void postConstruct() {
    this.registry.retry(SALADS_RETRY_NAME).getEventPublisher().onRetry(this::logRetry);
    this.registry.retry(SALADS_RETRY_NAME).getEventPublisher().onSuccess(this::logSuccess);
    this.registry.retry(SALADS_RETRY_NAME).getEventPublisher().onError(this::logError);
  }

  private void logRetry(final RetryOnRetryEvent event) {
    log.log(INFO, "Salads were not received after {0} retries!",
        event.getNumberOfRetryAttempts());
  }

  private void logSuccess(final RetryOnSuccessEvent event) {
    log.log(INFO, "Salads were received successfully after {0} retries.",
        event.getNumberOfRetryAttempts());
  }

  private void logError(final RetryOnErrorEvent event) {
    log.log(WARNING, "Salads were not received after {0}-retries and failed on following error : {1}",
        new Object[]{event.getNumberOfRetryAttempts(), event.getLastThrowable()});
  }
}
