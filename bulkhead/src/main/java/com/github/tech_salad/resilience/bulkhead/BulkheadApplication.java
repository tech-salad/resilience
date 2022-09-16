package com.github.tech_salad.resilience.bulkhead;

import com.github.tech_salad.resilience.bulkhead.client.DrinkRestClient;
import com.github.tech_salad.resilience.bulkhead.client.SaladRestClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BulkheadApplication {
	public static void main(String[] args) {
		SpringApplication.run(BulkheadApplication.class, args);
	}
}