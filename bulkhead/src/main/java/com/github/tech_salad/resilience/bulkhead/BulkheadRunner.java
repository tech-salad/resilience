package com.github.tech_salad.resilience.bulkhead;

import com.github.tech_salad.resilience.bulkhead.client.DrinkRestClient;
import com.github.tech_salad.resilience.bulkhead.client.SaladRestClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class BulkheadRunner /*implements CommandLineRunner*/ {

	private DrinkRestClient drinkRestClient;
	private SaladRestClient saladRestClient;

//	@Override
	public void run(String... args) {
		saladRestClient.get();
	}

}