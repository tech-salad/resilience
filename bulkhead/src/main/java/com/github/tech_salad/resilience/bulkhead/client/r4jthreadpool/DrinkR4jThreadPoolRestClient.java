package com.github.tech_salad.resilience.bulkhead.client.r4jthreadpool;

import com.github.tech_salad.resilience.bulkhead.client.RestClient;
import com.github.tech_salad.resilience.bulkhead.config.RestEndpointConfiguration;
import com.github.tech_salad.resilience.bulkhead.model.Drink;
import com.github.tech_salad.resilience.bulkhead.model.DrinkList;
import com.github.tech_salad.resilience.bulkhead.model.Salad;
import com.github.tech_salad.resilience.bulkhead.model.SaladList;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Component("drinkR4jThreadPoolClient")
public class DrinkR4jThreadPoolRestClient implements RestClient<Drink>{

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RestEndpointConfiguration restEndpointConfiguration;


    @SneakyThrows
    @Override
    public List<Drink> get() {
        return getFuture().get();
    }

    @Bulkhead(name = "drinks", type = Bulkhead.Type.THREADPOOL)
    public CompletableFuture<List<Drink>> getFuture() {
        return CompletableFuture.supplyAsync(() -> {
            DrinkList drinks = restTemplate.getForObject(restEndpointConfiguration.getDrinksUrl(), DrinkList.class);
            return (drinks != null) ? drinks.getDrinks() : Collections.emptyList();
        });
    }
}
