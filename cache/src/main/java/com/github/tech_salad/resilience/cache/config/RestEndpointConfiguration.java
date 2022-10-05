package com.github.tech_salad.resilience.cache.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class RestEndpointConfiguration {

    @Value("${drinks.url}")
    private String drinksUrl;

}
