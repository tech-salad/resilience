package org.techsalad.timeout.controller;

import static java.util.logging.Level.WARNING;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.techsalad.timeout.service.SaladsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@RestController
@RequiredArgsConstructor
@Log
public class SaladsController {

  private final SaladsService saladsService;

  @Value("${techsalad.waitDuration}")
  private Duration waitDuration;

  @GetMapping("/salads")
  public List<String> getSalads() {
    try {
      return saladsService.getAllSalads().get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  @GetMapping("/salad/types")
  public List<String> getSaladsTypes() {
    try {
      return saladsService.getSaladTypes().get(waitDuration.toMillis(), TimeUnit.MILLISECONDS);
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    } catch (TimeoutException e) {
      log.log(WARNING, "Getting the salads reached the timeout!");
      throw new RuntimeException(e);
    }
  }

}

