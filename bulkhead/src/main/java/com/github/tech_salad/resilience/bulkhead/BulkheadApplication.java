package com.github.tech_salad.resilience.bulkhead;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BulkheadApplication {
	public static void main(String[] args) {
		SpringApplication.run(BulkheadApplication.class, args);
	}
}