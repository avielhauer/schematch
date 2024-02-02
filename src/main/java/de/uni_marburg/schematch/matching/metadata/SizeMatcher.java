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
public class SizeMatcher extends TablePairMatcher {

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
                if (sourceType != Datatype.INTEGER && sourceType != Datatype.FLOAT) {
                    simMatrix[i][j] = 0.0f;
                    continue;
                }
                if (targetType != Datatype.INTEGER && targetType != Datatype.FLOAT) {
                    simMatrix[i][j] = 0.0f;
                    continue;
                }
                float test = calculateScore(sourceTable.getColumn(i).getValues(), targetTable.getColumn(j).getValues());
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
            numSourceDigits = s.contains(".") ? s.length() - 1 : s.length();
            if (numSourceDigits > maxSourceDigits) {
                maxSourceDigits = numSourceDigits;
            }
        }

        for (String t : targetColumn) {
            if (t.isEmpty()) continue;
            numTargetDigits = t.contains(".") ? t.length() - 1 : t.length();
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