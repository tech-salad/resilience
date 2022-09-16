package com.github.tech_salad.resilience.bulkhead.client;

import com.github.tech_salad.resilience.bulkhead.config.RestConfiguration;
import com.github.tech_salad.resilience.bulkhead.model.Drink;
import com.github.tech_salad.resilience.bulkhead.model.Salad;
import com.github.tech_salad.resilience.bulkhead.model.SaladList;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;

@Component
@AllArgsConstructor
@Slf4j
public class SaladRestClient implements RestClient<Salad>{

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RestConfiguration restConfiguration;

    @PostConstruct
    public void postConstruct() {
        BulkheadRegistry registry = BulkheadRegistry.ofDefaults();
        io.github.resilience4j.bulkhead.Bulkhead bulkheadWithDefaultConfig = registry.bulkhead("salads");
        bulkheadWithDefaultConfig.getEventPublisher().onCallPermitted(event -> log.info("Call permitted: " + event));
        bulkheadWithDefaultConfig.getEventPublisher().onCallFinished(event -> log.info("Call finished: " + event));
        bulkheadWithDefaultConfig.getEventPublisher().onCallRejected(event -> log.info("Call rejected: " + event));
    }

    @Bulkhead(name = "salads", type = Bulkhead.Type.SEMAPHORE)
    public List<Salad> get() {
        SaladList salads = restTemplate.getForObject(restConfiguration.getSaladsUrl(), SaladList.class);
        return (salads != null) ? salads.getSalads() : Collections.emptyList();
    }
}
