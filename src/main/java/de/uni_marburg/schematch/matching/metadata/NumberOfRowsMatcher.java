package de.uni_marburg.schematch.matching.metadata;

import de.uni_marburg.schematch.data.Table;
import de.uni_marburg.schematch.matching.TablePairMatcher;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class NumberOfRowsMatcher extends TablePairMatcher {
    @Override
    public float[][] match(TablePair tablePair) {

        final float[][] simMatrix = tablePair.getEmptySimMatrix();
        final Table sourceTable = tablePair.getSourceTable();
        final Table targetTable = tablePair.getTargetTable();

        for (int i = 0; i < sourceTable.getNumColumns(); i++) {
            for (int j = 0; j < targetTable.getNumColumns(); j++) {

                final int sourceSize = sourceTable.getColumn(i).getValues().size();
                final int targetSize = targetTable.getColumn(j).getValues().size();
                float similarity;
                if (sourceSize > targetSize) {
                    similarity = (float) targetSize / sourceSize;
                } else {
                    similarity = (float) sourceSize / targetSize;
                }
                simMatrix[i][j] = similarity;
            }
        }

        return simMatrix;
    }

}
