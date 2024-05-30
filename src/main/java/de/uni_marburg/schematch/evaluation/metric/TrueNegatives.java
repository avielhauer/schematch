package de.uni_marburg.schematch.evaluation.metric;

public class TrueNegatives extends BinaryMetric{
    @Override
    public float run(int[] groundTruthVector, float[] simVector) {
        BinaryMetric.ConfusionMatrix results = getConfusionMatrix(groundTruthVector, simVector);
        return results.getTN();
    }

    public Float aggregatePerformance(float sumScores, int n){
        return sumScores;
    }
}
