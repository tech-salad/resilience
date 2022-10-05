package org.techsalad.resilience.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.techsalad.resilience.service.SaladEnum;

@RestController
@RequestMapping(path = "/feign")
@AllArgsConstructor
public class SaladFeignExampleController {


    @GetMapping(path = "salad/{type}")
    public ResponseEntity<String> salad(@PathVariable(name = "type") final SaladEnum salad){
        if (salad == SaladEnum.CHEESE) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Bad request!");
        }
        return ResponseEntity.status(HttpStatus.OK).body("Salad does not exist!");
    }

    @GetMapping(path = "dressing/{type}")
    public ResponseEntity<String> dressing(@PathVariable(name = "type") final SaladEnum salad){
        if (salad == SaladEnum.CHEESE) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Cannot order dressing!");
        }
        return ResponseEntity.status(HttpStatus.OK).body("Dressing does not exist!");
    }


}
