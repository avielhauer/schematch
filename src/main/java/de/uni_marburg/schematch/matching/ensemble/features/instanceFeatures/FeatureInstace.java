package de.uni_marburg.schematch.matching.ensemble.features.instanceFeatures;

import de.uni_marburg.schematch.matching.ensemble.features.Feature;

import java.util.List;
import java.util.stream.Collectors;

public abstract class FeatureInstace extends Feature {
    public List<Double> calculateAverage(List<List<Double>> toAverage){
        return toAverage.stream()
                .map(innerList -> innerList.stream()
                        .mapToDouble(Double::doubleValue)
                        .average() // Durchschnitt berechnen
                        .orElse(0.0)) // Default-Wert, falls Liste leer ist
                .collect(Collectors.toList());
    }
}
