package de.uni_marburg.schematch.evaluation.metric;

public class Precision extends BinaryMetric {
    @Override
    public float run(int[] groundTruthVector, float[] simVector) {
        ConfusionMatrix results = getConfusionMatrix(groundTruthVector, simVector);
        return results.precision();
    }
}
