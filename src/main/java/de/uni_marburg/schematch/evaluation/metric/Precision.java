package de.uni_marburg.schematch.evaluation.metric;

public class Precision extends Metric{

    @Override
    public float run(int[] groundTruthVector, float[] simVector) {
        float threshold = 0.7f;
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
        if(tp == 0 && fp == 0) return 0.0f;
        return ((float) tp) /((float) (tp+fp));
    }
}
