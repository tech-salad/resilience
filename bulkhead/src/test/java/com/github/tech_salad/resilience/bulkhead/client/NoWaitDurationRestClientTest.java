package com.github.tech_salad.resilience.bulkhead.client;

import com.github.tech_salad.resilience.bulkhead.BulkheadApplication;
import com.github.tech_salad.resilience.bulkhead.config.RestEndpointConfiguration;
import com.github.tech_salad.resilience.bulkhead.model.Salad;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith({SpringExtension.class})
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        classes = BulkheadApplication.class,
        properties = {"resilience4j.bulkhead.instances.salads.maxWaitDuration=0"}
)
@Slf4j
public class NoWaitDurationRestClientTest {

    @RegisterExtension
    static WireMockExtension wireMockExtension = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort().dynamicHttpsPort())
            .build();

    @MockBean
    private RestEndpointConfiguration restEndpointConfiguration;

    private final static int REST_CALL_DURATION_IN_SECONDS = 1;
    private final static int REST_CALL_DURATION_IN_MILIS = REST_CALL_DURATION_IN_SECONDS * 1000;
    private final static int ASYNC_TASKS_COUNT = 12;

    @BeforeEach
    public void beforeEach() {
        when(restEndpointConfiguration.getSaladsUrl()).thenReturn("http://127.0.0.1:" + wireMockExtension.getRuntimeInfo().getHttpPort() + "/salads");
        wireMockExtension.stubFor(get(urlEqualTo("/salads"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withFixedDelay(REST_CALL_DURATION_IN_MILIS)
                        .withBody(" { \"salads\" : [ { \"name\" : \"Creamy Vegan Pasta Salad\" } , { \"name\" : \"Shredded Brussels Sprout Salad\" } ] }")));

        log.info("Init done");
    }

    @Autowired
    @Qualifier("saladR4jClient")
    private RestClient<Salad> saladR4jRestClient;

    @Test
    void testMaxWaitDurationExceeded() {
        Exception thrown = assertThrows(CompletionException.class, () -> {
            // when
            RestClientTest.invokeParallel(saladR4jRestClient, ASYNC_TASKS_COUNT)
                    .stream().forEach(CompletableFuture::join);

        }, "Bulkhead 'salads' is full and does not permit further calls");

        assertEquals(BulkheadFullException.class, thrown.getCause().getClass());
    }

}
