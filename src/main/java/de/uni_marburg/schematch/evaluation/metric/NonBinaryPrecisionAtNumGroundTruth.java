package de.uni_marburg.schematch.evaluation.metric;

import de.uni_marburg.schematch.evaluation.Evaluator;
import de.uni_marburg.schematch.evaluation.performance.Performance;

import java.util.Arrays;

public class NonBinaryPrecisionAtNumGroundTruth extends Metric {
    @Override
    public float run(int[] groundTruthVector, float[] simVector) {
        int numGT = 0;
        for (int i : groundTruthVector) {
            numGT += i;
        }

        float totalSimScoreTP = 0;
        float totalSimScoreFP = 0;

        float[] sortedSimVector = simVector.clone();
        Arrays.sort(sortedSimVector);

        float simScoreAtNumGT = sortedSimVector[numGT];

        // flag all scores >= simScoreAtNumGT as TP/FP
        for (int i = 0; i < groundTruthVector.length; i++) {
            float simScore = simVector[i];
            if (simScore >= simScoreAtNumGT) {
                if (groundTruthVector[i] == 1) {
                    totalSimScoreTP += simScore;
                } else {
                    totalSimScoreFP += simScore;
                }
            }
        }

        float score = 0f;
        if (totalSimScoreTP + totalSimScoreFP > 0) {
            score = totalSimScoreTP / (totalSimScoreTP + totalSimScoreFP);
        }

        return score;
    }
}
