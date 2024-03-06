package de.uni_marburg.schematch.evaluation.metric;

import org.apache.commons.lang3.NotImplementedException;

public class ProfilingRuntime extends Metric {

    @Override
    public boolean runsOnSimilarityMatrices() {
        return false;
    }

    @Override
    public float run(int[] groundTruthVector, float[] simVector) {
        throw new NotImplementedException("This metric should not be calculated on similarity matrices.");
    }
}
