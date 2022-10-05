package com.github.tech_salad.resilience.cache.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DrinkList {

    @Builder.Default
    private List<Drink> drinks = new ArrayList<>();
}
