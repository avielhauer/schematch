package de.uni_marburg.schematch.matching.metadata;

import de.uni_marburg.schematch.data.Table;
import de.uni_marburg.schematch.matching.TablePairMatcher;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)

public class MinimumLength extends TablePairMatcher {

    @Override
    public float[][] match(TablePair tablePair) {
        Table sourceTable = tablePair.getSourceTable();
        Table targetTable = tablePair.getTargetTable();
        float[][] simMatrix = tablePair.getEmptySimMatrix();
        for (int i = 0; i < sourceTable.getNumColumns(); i++) {
            for (int j = 0; j < targetTable.getNumColumns(); j++) {
                simMatrix[i][j] = calculateScore(sourceTable.getColumn(i).getValues(), targetTable.getColumn(j).getValues());
            }
        }
        return simMatrix;
    }

    private float calculateScore(List<String> sourceColumn, List<String> targetColumn) {

        int minSourceDigits = Integer.MAX_VALUE;
        int minTargetDigits = Integer.MAX_VALUE;
        int numSourceDigits;
        int numTargetDigits;

        for (String s : sourceColumn) {
            if (s.isEmpty()) continue;
            numSourceDigits = s.length();
            if (numSourceDigits < minSourceDigits) {
                minSourceDigits = numSourceDigits;
            }
        }

        for (String t : targetColumn) {
            if (t.isEmpty()) continue;
            numTargetDigits = t.length();
            if (numTargetDigits < minTargetDigits) {
                minTargetDigits = numTargetDigits;
            }
        }

        if (minTargetDigits > minSourceDigits) {
            return (float) (minSourceDigits / minTargetDigits);
        }
        return (float) minTargetDigits / minSourceDigits;
    }

}
