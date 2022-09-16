package com.github.tech_salad.resilience.bulkhead.client;

import com.github.tech_salad.resilience.bulkhead.BulkheadApplication;
import com.github.tech_salad.resilience.bulkhead.config.RestConfiguration;
import com.github.tech_salad.resilience.bulkhead.model.Drink;
import com.github.tech_salad.resilience.bulkhead.model.Salad;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@ExtendWith({SpringExtension.class})
@SpringBootTest(
        webEnvironment = WebEnvironment.NONE,
        classes = BulkheadApplication.class
        )
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

  private final static int REST_CALL_DURATION_IN_SECONDS = 1;
  private final static int REST_CALL_DURATION_IN_MILIS = REST_CALL_DURATION_IN_SECONDS * 1000;
  private final static int ASYNC_TASKS_COUNT = 12;

  private final static int CALL_OVERHEAD_DURATION = 1;
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

    log.info("Init done");
  }

  @Test
  void testGetSaladsOnceOK() {
    LocalDateTime startTime = LocalDateTime.now();

    // when
    List<Salad> salads = saladRestClient.get();
    log.info("{}", salads);

    LocalDateTime endTime = LocalDateTime.now();
    int duration = (int) Duration.between(startTime, endTime).getSeconds();

    // then
    assertEquals(2, salads.size());

    assertThat("Bulkhead concurrency = 2, so we should not have executed all in one shot",
            duration,
            lessThanOrEqualTo(REST_CALL_DURATION_IN_SECONDS + CALL_OVERHEAD_DURATION));
  }

  @Test
  void testGetSaladsExceedMaxCapacityPostponesCalls() throws ExecutionException, InterruptedException {
    LocalDateTime startTime = LocalDateTime.now();

    // when
    invokeParallel(saladRestClient, ASYNC_TASKS_COUNT)
            .stream().forEach(CompletableFuture::join);

    LocalDateTime endTime = LocalDateTime.now();
    int duration = (int) Duration.between(startTime, endTime).getSeconds();

    assertThat("Bulkhead concurrency = 2, so we should not have exceeded time needed for sequential retrieval",
            duration,
            lessThan(ASYNC_TASKS_COUNT * REST_CALL_DURATION_IN_SECONDS));
    assertThat("Bulkhead concurrency = 2, so we should not have executed all in one shot",
            duration,
            greaterThan(REST_CALL_DURATION_IN_SECONDS + CALL_OVERHEAD_DURATION));
  }

  private <T> List<CompletableFuture> invokeParallel(RestClient<T> restClient, int taskCountToSubmit) throws InterruptedException, ExecutionException {
    List<CompletableFuture> futures = new ArrayList<>();

    for (int i = 0; i < taskCountToSubmit; i++) {
      futures.add(CompletableFuture
              .supplyAsync(() -> {
                List<T> list = restClient.get();
                log.info("{}", list);
                return list;
              }));
    }

    return futures;
  }

  @Test
  void testGetSaladsAndGetDrinksIndependantCalls() throws ExecutionException, InterruptedException {
    LocalDateTime startTime = LocalDateTime.now();

    // when
    Stream.of(
                    invokeParallel(saladRestClient, ASYNC_TASKS_COUNT / 2),
                    invokeParallel(drinkRestClient, ASYNC_TASKS_COUNT / 2))
            .flatMap(List::stream)
            .forEach(CompletableFuture::join);

    LocalDateTime endTime = LocalDateTime.now();
    int duration = (int) Duration.between(startTime, endTime).getSeconds();

    assertThat("Bulkhead concurrency = 2, so we should not have exceeded time needed for sequential retrieval",
            duration,
            lessThan( 1 + ASYNC_TASKS_COUNT * REST_CALL_DURATION_IN_SECONDS / 4));
    assertThat("Bulkhead concurrency = 2, so we should not have executed all in one shot",
            duration,
            greaterThan(REST_CALL_DURATION_IN_SECONDS + CALL_OVERHEAD_DURATION));
  }
}