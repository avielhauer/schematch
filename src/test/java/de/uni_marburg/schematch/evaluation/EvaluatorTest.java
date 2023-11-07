package de.uni_marburg.schematch.evaluation;

import de.uni_marburg.schematch.evaluation.performance.TablePairPerformance;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EvaluatorTest {

    @Test
    void evaluateMatrix() {
        float[][] simMatrix = {
                {0.5f,0.7f},
                {0.2f,0.9f}
        };
        int[][] gtMatrix = {
                {1,0},
                {0,1}
        };

        TablePairPerformance tpp = Evaluator.evaluateMatrix(simMatrix, gtMatrix);

        float tppPrecision = (float) 2/3;
        float tppNonBinaryPrecision = (simMatrix[0][0] + simMatrix[1][1])/(simMatrix[0][0] + simMatrix[0][1] + simMatrix[1][1]);

        assertEquals(tppPrecision,tpp.calculatePrecision());
        assertEquals(tppNonBinaryPrecision,tpp.calculateNonBinaryPrecision());
    }
}