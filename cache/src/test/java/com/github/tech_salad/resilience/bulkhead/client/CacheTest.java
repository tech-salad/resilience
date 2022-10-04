package com.github.tech_salad.resilience.bulkhead.client;

import com.github.tech_salad.resilience.cache.CacheApplication;
import com.github.tech_salad.resilience.cache.client.RestClient;
import com.github.tech_salad.resilience.cache.config.RestEndpointConfiguration;
import com.github.tech_salad.resilience.cache.model.Drink;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@ExtendWith({SpringExtension.class})
@SpringBootTest(
        webEnvironment = WebEnvironment.NONE,
        classes = CacheApplication.class
)
@Slf4j
class CacheTest {

    @RegisterExtension
    static WireMockExtension wireMockExtension = WireMockExtension.newInstance()
            .options(wireMockConfig()
                    .dynamicPort()
                    .dynamicHttpsPort()
                    // verbose logging
                    .notifier(new ConsoleNotifier(true))
                    // response templating
                    .extensions(new ResponseTemplateTransformer(false)))
            .build();

    @Autowired
    private RestClient<Drink> drinkRestClient;

    @MockBean
    private RestEndpointConfiguration restEndpointConfiguration;

    @Autowired
    CacheManager cacheManager;

    private void evictAllCacheValues(String cacheName) {
        cacheManager.getCache(cacheName).clear();
    }

    @BeforeEach
    public void beforeEach() {
        // given
        when(restEndpointConfiguration.getDrinksUrl()).thenReturn("http://127.0.0.1:" + wireMockExtension.getRuntimeInfo().getHttpPort() + "/drinks");

        wireMockExtension.stubFor(get(urlEqualTo("/drinks"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(" { \"drinks\" : [ { \"name\" : \"Beer\", \"caloriesPer100g\" : \"43300\" } , { \"name\" : \"Wine\", \"caloriesPer100g\" : \"82900\" } ] }")));

        evictAllCacheValues("drinks");
    }

    @Test
    void testGetAllFromCache() {
        // when
        List<Drink> drinks = drinkRestClient.getAll();
        assertEquals(2, drinks.size());

        drinks = drinkRestClient.getAll();
        assertEquals(2, drinks.size());

        // then
        wireMockExtension.verify(1, getRequestedFor(urlEqualTo("/drinks")));
    }

    @Test
    void testSaveAllClearsCache() {
        // given
        wireMockExtension.stubFor(post(urlEqualTo("/drinks"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{{{request.body}}}")
                        .withTransformers("response-template")
                ));

        // when
        List<Drink> drinks = drinkRestClient.getAll();
        assertEquals(2, drinks.size());

        List<Drink> drinksPosted = drinkRestClient.saveAll(Collections.singletonList(Drink.builder().name("Watter").build()));
        assertEquals(1, drinksPosted.size());

        drinks = drinkRestClient.getAll();
        assertEquals(2, drinks.size());

        // then
        wireMockExtension.verify(2, getRequestedFor(urlEqualTo("/drinks")));
    }

    @Test
    void testSaveAndGetFromCache() {
        // given
        wireMockExtension.stubFor(post(urlEqualTo("/drinks/Water"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{{{request.body}}}")
                        .withTransformers("response-template")
                ));

        // when
        Drink drinksPosted = drinkRestClient.save(Drink.builder().name("Water").caloriesPer100g(0).build());
        assertEquals("Water", drinksPosted.getName());

        Drink drink = drinkRestClient.get("Water");
        assertEquals(0, drink.getCaloriesPer100g());

        // then
        wireMockExtension.verify(1, postRequestedFor(urlEqualTo("/drinks/Water")));
        wireMockExtension.verify(0, getRequestedFor(urlEqualTo("/drinks")));
    }
}
