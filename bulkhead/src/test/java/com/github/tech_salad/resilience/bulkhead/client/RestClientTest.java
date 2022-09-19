package com.github.tech_salad.resilience.bulkhead.client;

import com.github.tech_salad.resilience.bulkhead.BulkheadApplication;
import com.github.tech_salad.resilience.bulkhead.config.RestEndpointConfiguration;
import com.github.tech_salad.resilience.bulkhead.model.Drink;
import com.github.tech_salad.resilience.bulkhead.model.Salad;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@ExtendWith({SpringExtension.class})
@SpringBootTest(
        webEnvironment = WebEnvironment.NONE,
        classes = BulkheadApplication.class
        )
@Slf4j
@TestInstance(PER_CLASS) // to enable non-static method referred in @MethodSource
class RestClientTest {

  @RegisterExtension
  static WireMockExtension wireMockExtension = WireMockExtension.newInstance()
          .options(wireMockConfig().dynamicPort().dynamicHttpsPort())
          .build();

  @Autowired
  @Qualifier("drinkR4jClient")
  private RestClient<Drink> drinkR4jRestClient;

  @Autowired
  @Qualifier("saladR4jClient")
  private RestClient<Salad> saladR4jRestClient;

  @Autowired
  @Qualifier("drinkR4jThreadPoolClient")
  private RestClient<Drink> drinkR4jThreadPoolRestClient;

  @Autowired
  @Qualifier("saladR4jThreadPoolClient")
  private RestClient<Salad> saladR4jThreadPoolRestClient;

  @Autowired
  @Qualifier("drinkThreadPoolClient")
  private RestClient<Drink> drinkThreadPoolRestClient;

  @Autowired
  @Qualifier("saladThreadPoolClient")
  private RestClient<Salad> saladThreadPoolRestClient;

  @MockBean
  private RestEndpointConfiguration restEndpointConfiguration;

  private final static int REST_CALL_DURATION_IN_SECONDS = 1;
  private final static int REST_CALL_DURATION_IN_MILIS = REST_CALL_DURATION_IN_SECONDS * 1000;
  private final static int ASYNC_TASKS_COUNT = 12;

  private final static int CALL_OVERHEAD_DURATION = 1;
  @BeforeEach
  public void beforeEach() {
    when(restEndpointConfiguration.getDrinksUrl()).thenReturn("http://127.0.0.1:" + wireMockExtension.getRuntimeInfo().getHttpPort() + "/drinks");
    wireMockExtension.stubFor(get(urlEqualTo("/drinks"))
            .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withFixedDelay(REST_CALL_DURATION_IN_MILIS)
                    .withBody(" { \"drinks\" : [ { \"name\" : \"Beer\" } , { \"name\" : \"Wine\" } ] }")));

    when(restEndpointConfiguration.getSaladsUrl()).thenReturn("http://127.0.0.1:" + wireMockExtension.getRuntimeInfo().getHttpPort() + "/salads");
    wireMockExtension.stubFor(get(urlEqualTo("/salads"))
            .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withFixedDelay(REST_CALL_DURATION_IN_MILIS)
                    .withBody(" { \"salads\" : [ { \"name\" : \"Creamy Vegan Pasta Salad\" } , { \"name\" : \"Shredded Brussels Sprout Salad\" } ] }")));

    log.info("Init done");
  }

  private Stream<Arguments> provideArguments() {
    return Stream.of(
            Arguments.of(this.drinkR4jRestClient, this.saladR4jRestClient, "resilience4j-semaphore"),
            Arguments.of(this.drinkR4jThreadPoolRestClient, this.saladR4jThreadPoolRestClient, "resilience4j-threadpool"),
            Arguments.of(this.drinkThreadPoolRestClient, this.saladThreadPoolRestClient, "threadpool")
    );
  }
  @ParameterizedTest(name = "[{index}] => {2}")
  @MethodSource("provideArguments")
  void testGetSaladsOnceOK(RestClient<Drink> drinkRestClient, RestClient<Salad> saladRestClient, String testId) {
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

  @ParameterizedTest(name = "[{index}] => {2}")
  @MethodSource("provideArguments")
  void testGetSaladsExceedMaxCapacityPostponesCalls(RestClient<Drink> drinkRestClient, RestClient<Salad> saladRestClient, String testId) {
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


  @ParameterizedTest(name = "[{index}] => {2}")
  @MethodSource("provideArguments")
  void testGetSaladsAndGetDrinksIndependantCalls(RestClient<Drink> drinkRestClient, RestClient<Salad> saladRestClient, String testId) {
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

  static <T> List<CompletableFuture> invokeParallel(RestClient<T> restClient, int taskCountToSubmit) {
    return Stream.generate(
            () -> CompletableFuture.supplyAsync(() -> {
              List<T> list = restClient.get();
              log.info("{}", list);
              return list;
            }))
            .limit(taskCountToSubmit)
            .collect(Collectors.toList());
  }
}
