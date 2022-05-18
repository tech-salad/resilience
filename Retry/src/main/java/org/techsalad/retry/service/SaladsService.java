package org.techsalad.retry.service;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class SaladsService {

  private int failuresCount = 0;

  public List<String> getAllSalads() {
    if (failuresCount < 3) {
      failuresCount++;
      throw new IllegalStateException("Bola kucapaca! Neviem najsť ani jeden šalát. :(");
    }
    failuresCount = 0;

    return Collections.singletonList("Najsamlepší šalátik!");
  }

}
