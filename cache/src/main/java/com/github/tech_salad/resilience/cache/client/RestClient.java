package com.github.tech_salad.resilience.cache.client;

import com.github.tech_salad.resilience.cache.model.Drink;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;

public interface RestClient<T> {

    List<T> getAll();

    @CacheEvict(value="drinks", allEntries=true)
    List<T> saveAll(List<T> drinks);

    @CachePut(value="addresses")
    Drink save(Drink drink);

    @Cacheable(value="#name")
    Drink get(String name);
}
