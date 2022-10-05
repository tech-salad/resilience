package org.techsalad.feign.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.techsalad.feign.client.SaladClient;

@RestController
@RequestMapping(path = "/feign")
@AllArgsConstructor
public class SaladController {
    private final SaladClient saladClient;

    @GetMapping(path = "salad/{type}")
    public ResponseEntity<String> salad(@PathVariable(name = "type") final String salad){
        return saladClient.salad(salad);
    }

    @GetMapping(path = "dressing/{type}")
    public ResponseEntity<String> dressing(@PathVariable(name = "type") final String dressing){
        return saladClient.dressing(dressing);
    }
}
