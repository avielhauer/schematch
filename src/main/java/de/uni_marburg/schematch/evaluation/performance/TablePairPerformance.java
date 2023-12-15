package de.uni_marburg.schematch.evaluation.performance;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TablePairPerformance {
    private int TP;
    private int FP;
    private float simScoreTP;
    private float simScoreFP;

    public float calculatePrecision() {
        return (float) TP / (TP + FP);
    }
    public float calculateNonBinaryPrecision() {
        if (simScoreTP + simScoreFP == 0) {
            return 0;
        } else {
            return simScoreTP / (simScoreTP + simScoreFP);
        }
    }
}
