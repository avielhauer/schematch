package de.uni_marburg.schematch.evaluation;

import de.uni_marburg.schematch.data.Column;
import de.uni_marburg.schematch.evaluation.performance.*;
import de.uni_marburg.schematch.matchtask.columnpair.ColumnPair;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

@Data
@NoArgsConstructor
public class Evaluator {
    final static Logger log = LogManager.getLogger(Evaluator.class);

    public static TablePairPerformance evaluateMatrix(TablePair tablePair, float[][] simMatrix, int[][] gtMatrix) {
        int totalTP = 0;
        int totalFP = 0;
        float totalSimScoreTP = 0;
        float totalSimScoreFP = 0;
        Map<ColumnPair, ColumnPairPerformance> columnPairPerformances = new HashMap<>();

        int numRows = gtMatrix.length;
        int numCols = gtMatrix[0].length;

        List<Column> sourceColumns = tablePair.getSourceTable().getColumns();
        List<Column> targetColumns = tablePair.getTargetTable().getColumns();

        for (int i = 0; i < numRows; i++) {
            // skip ground truth rows without an assignment (all ground truth values in row are zero)
            if (Arrays.stream(gtMatrix[i]).sum() == 0) {
                continue;
            }
            if (Arrays.stream(gtMatrix[i]).sum() > 1) {
                log.warn("Found 1:n or m:n ground truth matches; those are not supported yet");
            }
            for (int j = 0; j < numCols; j++) {
                if (gtMatrix[i][j] == 1) {
                    float[] simRow = simMatrix[i];
                    float gtSimScore = simRow[j];
                    int numCheckedColumns = 0;
                    float sumSimScore = 0;
                    for (int jj = 0; jj < numCols; jj++) {
                        // sim scores equal to the ground truth should be included as false positives
                        // (e.g., [0.0, 0.0, 0.0] yields 2 false positives)
                        if (simRow[jj] >= gtSimScore) {
                            numCheckedColumns += 1;
                            sumSimScore += simRow[jj];
                        }
                    }
                    int TP = 1;
                    int FP = numCheckedColumns - 1;
                    float simScoreTP = gtSimScore;
                    float simscoreFP = sumSimScore - gtSimScore;
                    ColumnPair columnPair = new ColumnPair(sourceColumns.get(i), targetColumns.get(j));
                    ColumnPairPerformance columnPairPerformance = new ColumnPairPerformance(TP, FP, simScoreTP, simscoreFP);
                    totalTP += TP;
                    totalFP += FP;
                    totalSimScoreTP += simScoreTP;
                    totalSimScoreFP += simscoreFP;
                    columnPairPerformances.put(columnPair, columnPairPerformance);
                    break;
                }
            }
        }

        return new TablePairPerformance(totalTP, totalFP, totalSimScoreTP, totalSimScoreFP, columnPairPerformances);
    }
}
