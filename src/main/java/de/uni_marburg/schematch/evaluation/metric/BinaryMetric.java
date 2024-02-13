package de.uni_marburg.schematch.evaluation.metric;

import lombok.Data;

public abstract class BinaryMetric extends Metric {
    @Data
    protected static class ConfusionMatrix {
        float TP;
        float FP;
        float TN;
        float FN;

        public float f1() {
            if (precision() + recall() > 0) {
                return 2 * precision() * recall() / (precision() + recall());
            }

            return 0f;
        }

        public float precision() {
            if (TP + FP > 0) {
                return TP / (TP + FP);
            }

            return 0f;
        }

        public float recall() {
            if (TP + FN > 0) {
                return TP / (TP + FN);
            }

            return 0f;
        }

        public float accuracy() {
            if (TP + FP + TN + FN > 0) {
                return (TP + TN) / (TP + FP + TN + FN);
            }

            return 0f;
        }
    }

    protected ConfusionMatrix getConfusionMatrix(int[] groundTruthVector, float[] simVector) {
        ConfusionMatrix results = new ConfusionMatrix();
        
        for (int i = 0; i < groundTruthVector.length; i++) {
            float simScore = simVector[i];
            if (simScore >= 0.5) {
                if (groundTruthVector[i] == 1) {
                    results.TP += 1;
                } else {
                    results.FP += 1;
                }
            } else {
                if (groundTruthVector[i] == 1) {
                    results.FN += 1;
                } else {

                    results.TN += 1;
                }
            }
        }

        return results;
    }
}
