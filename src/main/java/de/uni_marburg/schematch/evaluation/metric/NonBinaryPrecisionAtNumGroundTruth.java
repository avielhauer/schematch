package de.uni_marburg.schematch.evaluation.metric;

import de.uni_marburg.schematch.evaluation.performance.Performance;

public class NonBinaryPrecisionAtNumGroundTruth extends Metric {
    @Override
    public Performance run(int[] groundTruthVector, float[] simVector) {
        return new Performance(0.42f);
    }
}
