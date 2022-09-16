package com.github.tech_salad.resilience.bulkhead.client;

import com.github.tech_salad.resilience.bulkhead.config.RestConfiguration;
import com.github.tech_salad.resilience.bulkhead.model.Drink;
import com.github.tech_salad.resilience.bulkhead.model.DrinkList;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Component
@AllArgsConstructor
public class DrinkRestClient implements RestClient<Drink> {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RestConfiguration restConfiguration;


    @Bulkhead(name = "drinks", type = Bulkhead.Type.SEMAPHORE)
    public List<Drink> get() {
        DrinkList drinks = restTemplate.getForObject(restConfiguration.getDrinksUrl(), DrinkList.class);
        return (drinks != null) ? drinks.getDrinks() : Collections.emptyList();
    }
}
