package de.uni_marburg.schematch.matching.metadata;

import de.uni_marburg.schematch.data.Column;
import de.uni_marburg.schematch.data.Table;
import de.uni_marburg.schematch.data.metadata.Datatype;
import de.uni_marburg.schematch.matching.TablePairMatcher;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)

public class MinimumLength extends TablePairMatcher{

    @Override
    public float[][] match(TablePair tablePair) {
        Table sourceTable = tablePair.getSourceTable();
        Table targetTable = tablePair.getTargetTable();
        float[][] simMatrix = tablePair.getEmptySimMatrix();
        for (int i = 0; i < sourceTable.getNumColumns(); i++) {
            Column sourceColumn = sourceTable.getColumn(i);
            Datatype sourceType = sourceColumn.getDatatype();
            for (int j = 0; j < targetTable.getNumColumns(); j++) {
                Column targetColumn = targetTable.getColumn(j);
                Datatype targetType = targetColumn.getDatatype();
                simMatrix[i][j] = calculateScore(sourceTable.getColumn(i).getValues(), targetTable.getColumn(j).getValues());
            }
        }
        return simMatrix;
    }

    private float calculateScore(List<String> sourceColumn, List<String> targetColumn) {

        int minSourceDigits = 100000;
        int minTargetDigits = 100000;
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
        if (minSourceDigits == 0) return 0.0f;
        return (float) minTargetDigits / minSourceDigits;
    }

}
