package de.uni_marburg.schematch.evaluation.metric;

public class Recall extends BinaryMetric {
    @Override
    public float run(int[] groundTruthVector, float[] simVector) {
        BinaryMetric.ConfusionMatrix results = getConfusionMatrix(groundTruthVector, simVector);
        return results.recall();
    }
}
