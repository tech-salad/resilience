package com.github.tech_salad.resilience.bulkhead.client.r4jsemaphore;

import com.github.tech_salad.resilience.bulkhead.client.RestClient;
import com.github.tech_salad.resilience.bulkhead.config.RestEndpointConfiguration;
import com.github.tech_salad.resilience.bulkhead.model.Salad;
import com.github.tech_salad.resilience.bulkhead.model.SaladList;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Component("saladR4jClient")
@Slf4j
public class SaladR4jSemaphoreRestClient implements RestClient<Salad> {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RestEndpointConfiguration restEndpointConfiguration;

    @Bulkhead(name = "salads", type = Bulkhead.Type.SEMAPHORE)
    @Override
    public List<Salad> get() {
        SaladList salads = restTemplate.getForObject(restEndpointConfiguration.getSaladsUrl(), SaladList.class);
        return (salads != null) ? salads.getSalads() : Collections.emptyList();
    }
}
