package de.uni_marburg.schematch.boosting.sf_algorithm;

import java.util.Map;

@FunctionalInterface
public interface FloodingStep {
    Map<ObjectPair, Float> apply(Map<ObjectPair, Float> defaultSimilarityMap, Map<ObjectPair, Float> similarityMap, PropagationGraph pGraph);
}
