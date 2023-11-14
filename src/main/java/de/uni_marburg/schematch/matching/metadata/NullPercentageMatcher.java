package de.uni_marburg.schematch.matching.metadata;

import de.uni_marburg.schematch.data.Table;
import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;

@Data
@EqualsAndHashCode(callSuper = true)
public class NullPercentageMatcher extends Matcher {

    @Override
    public float[][] match(TablePair tablePair) {

        final float[][] simMatrix = tablePair.getEmptySimMatrix();
        final Table sourceTable = tablePair.getSourceTable();
        final Table targetTable = tablePair.getTargetTable();

        for (int i = 0; i < sourceTable.getNumberOfColumns(); i++) {
            for (int j = 0; j < targetTable.getNumberOfColumns(); j++) {

                ArrayList<String> sourceValues = new ArrayList<>(sourceTable.getColumn(i).getValues());
                ArrayList<String> targetValues = new ArrayList<>(targetTable.getColumn(j).getValues());

                var similarity = calculateSimilarity(sourceValues, targetValues);

                simMatrix[i][j] = (float) similarity;
            }
        }

        return simMatrix;
    }

    private static double calculateSimilarity(ArrayList<String> sourceValues, ArrayList<String> targetValues) {
        final int sourceSize = sourceValues.size();
        final int targetSize = targetValues.size();

        int sourceNulls = 0;
        int targetNulls = 0;

        for (String sourceValue : sourceValues) {
            if (sourceValue == null || sourceValue.isEmpty()) sourceNulls++;
        }

        for (String targetValue : targetValues) {
            if (targetValue == null || targetValue.isEmpty()) targetNulls++;
        }

        double sourceNullPercentage = (double) sourceNulls / sourceSize;
        double targetNullPercentage = (double) targetNulls / targetSize;

        //result is rounded to 2 digits
        return Math.round(Math.abs(sourceNullPercentage - targetNullPercentage) * 100.0) / 100.0;
    }

}
