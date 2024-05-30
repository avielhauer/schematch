package de.uni_marburg.schematch.evaluation.metric;

public class FalseNegatives extends BinaryMetric{
    @Override
    public float run(int[] groundTruthVector, float[] simVector) {
        BinaryMetric.ConfusionMatrix results = getConfusionMatrix(groundTruthVector, simVector);
        return results.getFN();
    }

    public Float aggregatePerformance(float sumScores, int n){
        return sumScores;
    }
}
