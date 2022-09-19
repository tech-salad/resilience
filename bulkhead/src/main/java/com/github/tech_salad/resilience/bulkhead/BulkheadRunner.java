package com.github.tech_salad.resilience.bulkhead;

import com.github.tech_salad.resilience.bulkhead.client.r4jsemaphore.DrinkR4jSemaphoreRestClient;
import com.github.tech_salad.resilience.bulkhead.client.r4jsemaphore.SaladR4jSemaphoreRestClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class BulkheadRunner /*implements CommandLineRunner*/ {

	private DrinkR4jSemaphoreRestClient drinkR4JSemaphoreRestClient;
	private SaladR4jSemaphoreRestClient saladR4JSemaphoreRestClient;

//	@Override
	public void run(String... args) {
		saladR4JSemaphoreRestClient.get();
	}

}