package de.uni_marburg.schematch.evaluation.metric;

public class Accuracy extends BinaryMetric {
    @Override
    public float run(int[] groundTruthVector, float[] simVector) {
        ConfusionMatrix results = getConfusionMatrix(groundTruthVector, simVector);
        return results.accuracy();
    }
}
