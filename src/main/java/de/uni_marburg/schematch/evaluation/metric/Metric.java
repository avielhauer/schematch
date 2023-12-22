package de.uni_marburg.schematch.evaluation.metric;

import de.uni_marburg.schematch.evaluation.performance.Performance;

public abstract class Metric {
    public abstract Performance run(int[] groundTruthVector, float[] simVector);

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
