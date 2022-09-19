package org.techsalad.resilience.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.techsalad.resilience.service.SaladEnum;
import org.techsalad.resilience.service.SaladService;

import java.util.stream.IntStream;

@RestController
@RequestMapping(path = "/circuitbreaker")
@AllArgsConstructor
public class SaladController {

    private final SaladService tunaSaladService;
    private final SaladService ceasarSaladService;
    private final SaladService fruitSaladService;

    @GetMapping(path = "salad/{type}")
    public String salad(@PathVariable(name = "type") final SaladEnum salad){
        switch (salad){
            case TUNA: return this.tunaSaladService.saladType();
            case CEASAR: return this.ceasarSaladService.saladType();
            case FRUIT: return this.fruitSaladService.saladType();
            default: return "Salad does not exist!";
        }
    }

    @GetMapping(path = "dressing/{type}")
    public String drink(@PathVariable(name = "type") final SaladEnum salad){
        switch (salad){
            case TUNA: return this.tunaSaladService.dressingType();
            case CEASAR: return this.ceasarSaladService.dressingType();
            case FRUIT:
                IntStream.range(0,10).forEach(value -> this.fruitSaladService.dressingType());
                return this.fruitSaladService.dressingType();
            default: return "Dressing does not exist!";
        }
    }

    @GetMapping(path = "order/{type}")
    public String order(@PathVariable(name = "type") final SaladEnum salad){
        switch (salad){
            case TUNA: return this.tunaSaladService.summarizeOrder();
            case CEASAR: return this.ceasarSaladService.summarizeOrder();
            case FRUIT: return this.fruitSaladService.summarizeOrder();
            default: return "Could not process order!";
        }
    }

}
