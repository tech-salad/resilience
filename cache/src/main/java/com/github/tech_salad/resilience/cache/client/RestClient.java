package com.github.tech_salad.resilience.cache.client;

import com.github.tech_salad.resilience.cache.model.Drink;

import java.util.List;

public interface RestClient<T> {

    List<T> getAll();

    List<T> saveAll(List<T> drinks);

    Drink save(Drink drink);

    Drink get(String name);
}
