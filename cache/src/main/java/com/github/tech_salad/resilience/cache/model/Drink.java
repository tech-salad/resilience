package com.github.tech_salad.resilience.cache.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Drink {

    private String name;
    private int caloriesPer100g;
}
