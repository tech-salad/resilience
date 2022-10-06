package org.techsalad.feign.config;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public ErrorDecoder saladErrorDecoder(){
        return new SaladErrorDecoder();
    }
}
