package com.github.tech_salad.resilience.bulkhead.client;

import java.util.List;

public interface RestClient<T> {

    List<T> get();
}
