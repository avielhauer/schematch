package de.uni_marburg.schematch.matching.metadata;

import de.uni_marburg.schematch.data.Table;
import de.uni_marburg.schematch.matching.TablePairMatcher;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)

public class MaximumLength extends TablePairMatcher {

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

        int maxSourceDigits = 0;
        int maxTargetDigits = 0;
        int numSourceDigits;
        int numTargetDigits;

        for (String s : sourceColumn) {
            if (s.isEmpty()) continue;
            numSourceDigits = s.length();
            if (numSourceDigits > maxSourceDigits) {
                maxSourceDigits = numSourceDigits;
            }
        }

        for (String t : targetColumn) {
            if (t.isEmpty()) continue;
            numTargetDigits = t.length();
            if (numTargetDigits > maxTargetDigits) {
                maxTargetDigits = numTargetDigits;
            }
        }

        if (maxTargetDigits > maxSourceDigits) {
            return (float) (maxSourceDigits / maxTargetDigits);
        }
        if (maxSourceDigits == 0) return 0.0f;
        return (float) maxTargetDigits / maxSourceDigits;
    }
}

