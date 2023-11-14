package de.uni_marburg.schematch.matching.metadata;

import de.uni_marburg.schematch.data.Table;
import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class NumberOfRowsMatcher extends Matcher {
    @Override
    public float[][] match(TablePair tablePair) {

        final float[][] simMatrix = tablePair.getEmptySimMatrix();
        final Table sourceTable = tablePair.getSourceTable();
        final Table targetTable = tablePair.getTargetTable();

        for (int i = 0; i < sourceTable.getNumberOfColumns(); i++) {
            for (int j = 0; j < targetTable.getNumberOfColumns(); j++) {

                final int sourceSize = sourceTable.getColumn(i).getValues().size();
                final int targetSize = targetTable.getColumn(j).getValues().size();
                float similarity = sourceSize == targetSize
                        ? 1.0f
                        : 0.0f;

                simMatrix[i][j] = similarity;
            }
        }

        return simMatrix;
    }

}
