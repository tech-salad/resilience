package com.github.tech_salad.resilience.bulkhead.client.threadpool;

import com.github.tech_salad.resilience.bulkhead.client.RestClient;
import com.github.tech_salad.resilience.bulkhead.config.RestEndpointConfiguration;
import com.github.tech_salad.resilience.bulkhead.model.Drink;
import com.github.tech_salad.resilience.bulkhead.model.DrinkList;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Component("drinkThreadPoolClient")
public class DrinkThreadPoolRestClient implements RestClient<Drink> {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RestEndpointConfiguration restEndpointConfiguration;

    @Value("${resilience4j.bulkhead.configs.default.maxConcurrentCalls}")
    private int poolSize;

    private ExecutorService executorService;

    @PostConstruct
    public void postConstruct() {
        executorService = Executors.newFixedThreadPool(poolSize);
    }

    @SneakyThrows
    public List<Drink> get() {
        Future<List<Drink>> future = executorService.submit(() -> {
            DrinkList drinks = restTemplate.getForObject(restEndpointConfiguration.getDrinksUrl(), DrinkList.class);
            return (drinks != null) ? drinks.getDrinks() : Collections.emptyList();
        });
        return future.get();
    }
}
