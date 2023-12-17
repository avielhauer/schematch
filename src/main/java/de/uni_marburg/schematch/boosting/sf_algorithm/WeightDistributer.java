package de.uni_marburg.schematch.boosting.sf_algorithm;

import java.util.Map;

@FunctionalInterface
public interface WeightDistributer {
    float apply(WeightedEdge edge, PropagationGraph pGraph);
}
