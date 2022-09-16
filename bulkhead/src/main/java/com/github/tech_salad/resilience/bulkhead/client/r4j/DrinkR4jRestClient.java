package com.github.tech_salad.resilience.bulkhead.client.r4j;

import com.github.tech_salad.resilience.bulkhead.client.RestClient;
import com.github.tech_salad.resilience.bulkhead.config.RestEndpointConfiguration;
import com.github.tech_salad.resilience.bulkhead.model.Drink;
import com.github.tech_salad.resilience.bulkhead.model.DrinkList;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Component("drinkR4jClient")
public class DrinkR4jRestClient implements RestClient<Drink> {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RestEndpointConfiguration restEndpointConfiguration;


    @Bulkhead(name = "drinks", type = Bulkhead.Type.SEMAPHORE)
    public List<Drink> get() {
        DrinkList drinks = restTemplate.getForObject(restEndpointConfiguration.getDrinksUrl(), DrinkList.class);
        return (drinks != null) ? drinks.getDrinks() : Collections.emptyList();
    }
}
