package de.uni_marburg.schematch.matching.metadata;

import de.uni_marburg.schematch.data.Table;
import de.uni_marburg.schematch.data.metadata.Datatype;
import de.uni_marburg.schematch.matching.TablePairMatcher;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class DecimalsMatcher extends TablePairMatcher {

    @Override
    public float[][] match(TablePair tablePair) {
        Table sourceTable = tablePair.getSourceTable();
        Table targetTable = tablePair.getTargetTable();
        float[][] simMatrix = tablePair.getEmptySimMatrix();
        for (int i = 0; i < sourceTable.getNumColumns(); i++) {
            Datatype sourceType = sourceTable.getColumn(i).getDatatype();
            for (int j = 0; j < targetTable.getNumColumns(); j++) {
                Datatype targetType = targetTable.getColumn(j).getDatatype();
                if (sourceType != Datatype.FLOAT) {
                    simMatrix[i][j] = 0.0f;
                    continue;
                }
                if (targetType != Datatype.FLOAT) {
                    simMatrix[i][j] = 0.0f;
                    continue;
                }
                simMatrix[i][j] = calculateScore(sourceTable.getColumn(i).getValues(), targetTable.getColumn(j).getValues());
            }
        }
        return simMatrix;
    }

    private float calculateScore(List<String> sourceColumn, List<String> targetColumn) {

        int maxSourceDecimals = 0;
        int maxTargetDecimals = 0;
        int numSourceDecimals;
        int numTargetDecimals;

        for (String s : sourceColumn) {

            numSourceDecimals = s.length() - (s.indexOf('.') + 1);
            if (numSourceDecimals > maxSourceDecimals) {
                maxSourceDecimals = numSourceDecimals;
            }
        }

        for (String s : targetColumn) {

            numTargetDecimals = s.length() - (s.indexOf('.') + 1);
            if (numTargetDecimals > maxTargetDecimals) {
                maxTargetDecimals = numTargetDecimals;
            }
        }

        if (maxTargetDecimals > maxSourceDecimals) {
            return (float)(maxSourceDecimals / maxTargetDecimals);
        }
        else return (float)(maxTargetDecimals / maxSourceDecimals);
    }
}
