package com.github.tech_salad.resilience.bulkhead.client.r4j;

import com.github.tech_salad.resilience.bulkhead.client.RestClient;
import com.github.tech_salad.resilience.bulkhead.config.RestEndpointConfiguration;
import com.github.tech_salad.resilience.bulkhead.model.Salad;
import com.github.tech_salad.resilience.bulkhead.model.SaladList;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Component("saladR4jClient")
public class SaladR4jRestClient implements RestClient<Salad> {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RestEndpointConfiguration restEndpointConfiguration;

//    @PostConstruct
//    public void postConstruct() {
//        BulkheadRegistry registry = BulkheadRegistry.ofDefaults();
//        io.github.resilience4j.bulkhead.Bulkhead bulkheadWithDefaultConfig = registry.bulkhead("salads");
//        bulkheadWithDefaultConfig.getEventPublisher().onCallPermitted(event -> log.info("Call permitted: " + event));
//        bulkheadWithDefaultConfig.getEventPublisher().onCallFinished(event -> log.info("Call finished: " + event));
//        bulkheadWithDefaultConfig.getEventPublisher().onCallRejected(event -> log.info("Call rejected: " + event));
//    }

    @Bulkhead(name = "salads", type = Bulkhead.Type.SEMAPHORE)
    public List<Salad> get() {
        SaladList salads = restTemplate.getForObject(restEndpointConfiguration.getSaladsUrl(), SaladList.class);
        return (salads != null) ? salads.getSalads() : Collections.emptyList();
    }
}
