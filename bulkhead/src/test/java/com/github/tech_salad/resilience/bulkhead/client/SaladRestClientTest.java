package com.github.tech_salad.resilience.bulkhead.client;

import com.github.tech_salad.resilience.bulkhead.BulkheadApplication;
import com.github.tech_salad.resilience.bulkhead.config.RestConfiguration;
import com.github.tech_salad.resilience.bulkhead.model.Drink;
import com.github.tech_salad.resilience.bulkhead.model.Salad;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@ExtendWith({SpringExtension.class})
@SpringBootTest(
        webEnvironment = WebEnvironment.NONE,
        classes = BulkheadApplication.class
        )
@AutoConfigureWireMock(port = 0)
@Slf4j
class SaladRestClientTest {

  @RegisterExtension
  static WireMockExtension wireMockExtension = WireMockExtension.newInstance()
          .options(wireMockConfig().dynamicPort().dynamicHttpsPort())
          .build();

  @Autowired
  private RestClient<Drink> drinkRestClient;

  @Autowired
  private RestClient<Salad> saladRestClient;

  @MockBean
  private RestConfiguration restConfiguration;

  private ThreadPoolExecutor executor;

  private final static int REST_CALL_DURATION_IN_SECONDS = 1;
  private final static int REST_CALL_DURATION_IN_MILIS = REST_CALL_DURATION_IN_SECONDS * 1000;
  private final static int THREADPOOL_SIZE = 10;

  @BeforeEach
  public void beforeEach() {
    when(restConfiguration.getDrinksUrl()).thenReturn("http://127.0.0.1:" + wireMockExtension.getRuntimeInfo().getHttpPort() + "/drinks");
    wireMockExtension.stubFor(get(urlEqualTo("/drinks"))
            .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withFixedDelay(REST_CALL_DURATION_IN_MILIS)
                    .withBody(" { \"drinks\" : [ { \"name\" : \"Beer\" } , { \"name\" : \"Wine\" } ] }")));

    when(restConfiguration.getSaladsUrl()).thenReturn("http://127.0.0.1:" + wireMockExtension.getRuntimeInfo().getHttpPort() + "/salads");
    wireMockExtension.stubFor(get(urlEqualTo("/salads"))
            .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withFixedDelay(REST_CALL_DURATION_IN_MILIS)
                    .withBody(" { \"salads\" : [ { \"name\" : \"Creamy Vegan Pasta Salad\" } , { \"name\" : \"Shredded Brussels Sprout Salad\" } ] }")));

    executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(THREADPOOL_SIZE);

    log.info("Init done");
  }

  @Test
  void testGetSaladsOnceOK() {
    // when
    List<Salad> salads = saladRestClient.get();
    log.info("{}", salads);

    // then
    assertEquals(2, salads.size());
  }

  @Test
  void testGetSaladsExceedMaxCapacityPostponesCalls() throws ExecutionException, InterruptedException {
    LocalDateTime startTime = LocalDateTime.now();

    // when
    invokeParallel(saladRestClient, true, THREADPOOL_SIZE);

    LocalDateTime endTime = LocalDateTime.now();
    int duration = (int) Duration.between(startTime, endTime).getSeconds();

    assertThat("Bulkhead concurrency = 2, so we should not have exceeded time needed for sequential retrieval",
            duration,
            Matchers.lessThan(THREADPOOL_SIZE * REST_CALL_DURATION_IN_SECONDS));
    assertThat("Bulkhead concurrency = 2, so we should not have executed all in one shot",
            duration,
            Matchers.greaterThan(2 * REST_CALL_DURATION_IN_SECONDS));
  }

  private <T> void invokeParallel(RestClient<T> restClient, boolean waitForResponses, int taskCountToSubmit) throws InterruptedException, ExecutionException {
    List<Future<List<T>>> results = new ArrayList<>();
    for (int i = 0; i < taskCountToSubmit; i++) {
      Future<List<T>> result = executor.submit(() -> {
        List<T> list = restClient.get();
        log.info("{}", list);
        return list;
      });
      results.add(result);
    }

    if(waitForResponses) {
      for (Future<List<T>> result : results) {
        result.get();
      }
    }
  }

  @Test
  void testGetSaladsAndGetDrinksIndependantCalls() throws ExecutionException, InterruptedException {
    LocalDateTime startTime = LocalDateTime.now();

    // when
    invokeParallel(saladRestClient, false, THREADPOOL_SIZE / 2);
    invokeParallel(drinkRestClient, true, THREADPOOL_SIZE / 2);

    LocalDateTime endTime = LocalDateTime.now();
    int duration = (int) Duration.between(startTime, endTime).getSeconds();

    assertThat("Bulkhead concurrency = 2, so we should not have exceeded time needed for sequential retrieval",
            duration,
            Matchers.lessThan( THREADPOOL_SIZE * REST_CALL_DURATION_IN_SECONDS));
    assertThat("Bulkhead concurrency = 2, so we should not have executed all in one shot",
            duration,
            Matchers.greaterThan(2 * REST_CALL_DURATION_IN_SECONDS));
  }
}
