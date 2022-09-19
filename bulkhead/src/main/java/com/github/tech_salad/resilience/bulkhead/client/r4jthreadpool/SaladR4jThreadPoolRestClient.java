package com.github.tech_salad.resilience.bulkhead.client.r4jthreadpool;

import com.github.tech_salad.resilience.bulkhead.client.RestClient;
import com.github.tech_salad.resilience.bulkhead.config.RestEndpointConfiguration;
import com.github.tech_salad.resilience.bulkhead.model.Salad;
import com.github.tech_salad.resilience.bulkhead.model.SaladList;
import io.github.resilience4j.bulkhead.ThreadPoolBulkhead;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

@Component("saladR4jThreadPoolClient")
public class SaladR4jThreadPoolRestClient implements RestClient<Salad> {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RestEndpointConfiguration restEndpointConfiguration;

    @SneakyThrows
    @Override
    public List<Salad> get() {
        return getFuture().get();
    }

    @Bulkhead(name = "salads", type = Bulkhead.Type.THREADPOOL)
    public CompletableFuture<List<Salad>> getFuture() {
        return CompletableFuture.supplyAsync(() -> {
            SaladList salads = restTemplate.getForObject(restEndpointConfiguration.getSaladsUrl(), SaladList.class);
            return (salads != null) ? salads.getSalads() : Collections.emptyList();
        });
    }
}
