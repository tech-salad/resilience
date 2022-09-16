package com.github.tech_salad.resilience.bulkhead;

import com.github.tech_salad.resilience.bulkhead.client.r4j.DrinkR4jRestClient;
import com.github.tech_salad.resilience.bulkhead.client.r4j.SaladR4jRestClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class BulkheadRunner /*implements CommandLineRunner*/ {

	private DrinkR4jRestClient drinkR4jRestClient;
	private SaladR4jRestClient saladR4jRestClient;

//	@Override
	public void run(String... args) {
		saladR4jRestClient.get();
	}

}