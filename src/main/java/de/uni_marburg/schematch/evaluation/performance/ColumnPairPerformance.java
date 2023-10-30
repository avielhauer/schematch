package de.uni_marburg.schematch.evaluation.performance;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ColumnPairPerformance extends Performance {

    public ColumnPairPerformance() {
        super();
    }

    public ColumnPairPerformance(int TP, int FP, float simScoreTP, float simScoreFP) {
        super(TP, FP, simScoreTP, simScoreFP);
    }

}
