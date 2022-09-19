package com.github.tech_salad.resilience.bulkhead.model;

import lombok.Data;

import java.util.List;

@Data
public class SaladList {
    private List<Salad> salads;
}
