package de.uni_marburg.schematch.evaluation;

import de.uni_marburg.schematch.evaluation.performance.TablePairPerformance;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Data
public class Evaluator {
    final static Logger log = LogManager.getLogger(Evaluator.class);

    public static TablePairPerformance evaluateMatrix(float[][] simMatrix, int[][] gtMatrix) {
        int totalTP = 0;
        int totalFP = 0;
        float totalSimScoreTP = 0;
        float totalSimScoreFP = 0;

        int numRows = gtMatrix.length;
        int numCols = gtMatrix[0].length;

        // find the lowest ground truth score
        float lowestGTScore = Float.MAX_VALUE;
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                if (gtMatrix[i][j] == 1 && simMatrix[i][j] < lowestGTScore) {
                    lowestGTScore = simMatrix[i][j];
                }
            }
        }

        // flag all scores >= lowest ground truth score as TP/FP
        for (int i = 0; i < numRows; i++) {
           for (int j = 0; j < numCols; j++) {
               float simScore = simMatrix[i][j];
               if (simScore >= lowestGTScore) {
                   if (gtMatrix[i][j] == 1) {
                       totalTP += 1;
                       totalSimScoreTP += simScore;
                   } else {
                       totalFP += 1;
                       totalSimScoreFP += simScore;
                   }
               }
            }
        }

        return new TablePairPerformance(totalTP, totalFP, totalSimScoreTP, totalSimScoreFP);
    }
}
