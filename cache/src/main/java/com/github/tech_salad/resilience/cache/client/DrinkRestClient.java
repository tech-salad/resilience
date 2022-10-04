package com.github.tech_salad.resilience.cache.client;

import com.github.tech_salad.resilience.cache.config.RestEndpointConfiguration;
import com.github.tech_salad.resilience.cache.model.Drink;
import com.github.tech_salad.resilience.cache.model.DrinkList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Component
public class DrinkRestClient implements RestClient<Drink> {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RestEndpointConfiguration restEndpointConfiguration;

    @Cacheable("drinks")
    @Override
    public List<Drink> getAll() {
        DrinkList drinks = restTemplate.getForObject(restEndpointConfiguration.getDrinksUrl(), DrinkList.class);
        return (drinks != null) ? drinks.getDrinks() : Collections.emptyList();
    }

    @Cacheable(value="drinks", key = "#name")
    @Override
    public Drink get(String name) {
        Drink drink = restTemplate.getForObject(restEndpointConfiguration.getDrinksUrl() + "/{name}", Drink.class, name);
        return drink;
    }

    @CacheEvict(value="drinks", allEntries=true)
    @Override
    public List<Drink> saveAll(List<Drink> drinks) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<DrinkList> request = new HttpEntity<>(DrinkList.builder().drinks(drinks).build(), headers);

        DrinkList result = restTemplate.postForObject(restEndpointConfiguration.getDrinksUrl(), request, DrinkList.class);
        if (result == null) {
            result = new DrinkList();
        }
        return result.getDrinks();
    }

    @CachePut(value="drinks", key = "#drink.name")
    @Override
    public Drink save(Drink drink) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Drink> request = new HttpEntity<>(drink, headers);

        Drink result = restTemplate.postForObject(restEndpointConfiguration.getDrinksUrl() + "/" + drink.getName(), request, Drink.class);
        return result;
    }


}
