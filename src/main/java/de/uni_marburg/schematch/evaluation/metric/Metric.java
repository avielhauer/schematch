package de.uni_marburg.schematch.evaluation.metric;

public abstract class Metric {
    public abstract float run(int[] groundTruthVector, float[] simVector);

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
