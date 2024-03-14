package de.uni_marburg.schematch.evaluation.metric;

public class FScore extends Metric {
    float threshold = .5f;
    @Override
    public float run(int[] groundTruthVector, float[] simVector) {
        int factor = 1;
        int tp = 0;
        int fp = 0;
        int tn = 0;
        int fn = 0;
        for (int i = 0; i < groundTruthVector.length; i++) {
            boolean isPositive = simVector[i] >= threshold;
            if (groundTruthVector[i] == 1) {
                if (isPositive) {
                    tp++;
                } else {
                    fn++;
                }
            } else {
                if (isPositive) {
                    fp++;
                } else {
                    tn++;
                }
            }
        }
        if (tp == 0) return 0f;
        return ((float) (1+factor)*tp) /((float) (((1+factor)*tp)+(factor*fn)+fp));
    }
}
