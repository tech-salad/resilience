package com.github.tech_salad.resilience.bulkhead.client.threadpool;

import com.github.tech_salad.resilience.bulkhead.client.RestClient;
import com.github.tech_salad.resilience.bulkhead.config.RestEndpointConfiguration;
import com.github.tech_salad.resilience.bulkhead.model.Salad;
import com.github.tech_salad.resilience.bulkhead.model.SaladList;
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

@Component("saladThreadPoolClient")
public class SaladThreadPoolRestClient implements RestClient<Salad> {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RestEndpointConfiguration restEndpointConfiguration;

    @Value("${resilience4j.bulkhead.instances.salads.maxConcurrentCalls}")
    private int poolSize;

    private ExecutorService executorService;

    @PostConstruct
    public void postConstruct() {
        executorService = Executors.newFixedThreadPool(poolSize);
    }

    @SneakyThrows
    public List<Salad> get() {
        Future<List<Salad>> future = executorService.submit(() -> {
            SaladList salads = restTemplate.getForObject(restEndpointConfiguration.getSaladsUrl(), SaladList.class);
            return (salads != null) ? salads.getSalads() : Collections.emptyList();
        });
        return future.get();
    }
}
