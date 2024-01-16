package de.uni_marburg.schematch.matching.metadata;

import de.uni_marburg.schematch.data.Table;
import de.uni_marburg.schematch.matching.TablePairMatcher;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.HashSet;

@Data
@EqualsAndHashCode(callSuper = true)
public class UniquenessMatcher extends TablePairMatcher {

    @Override
    public float[][] match(TablePair tablePair) {

        final float[][] simMatrix = tablePair.getEmptySimMatrix();
        final Table sourceTable = tablePair.getSourceTable();
        final Table targetTable = tablePair.getTargetTable();

        for (int i = 0; i < sourceTable.getNumColumns(); i++) {
            for (int j = 0; j < targetTable.getNumColumns(); j++) {
                ArrayList<String> sourceValues = new ArrayList<>(sourceTable.getColumn(i).getValues());
                ArrayList<String> targetValues = new ArrayList<>(targetTable.getColumn(j).getValues());

                simMatrix[i][j] = calculateSimilarity(sourceValues, targetValues);
            }
        }

        return simMatrix;
    }


    private float calculateSimilarity(ArrayList<String> sourceValues, ArrayList<String> targetValues) {
        HashSet<String> sourceElements = new HashSet<>(sourceValues);
        HashSet<String> targetElements = new HashSet<>(targetValues);

        float sourceDistinctPercentage = (float) sourceElements.size() / sourceValues.size();
        float targetDistinctPercentage = (float) targetElements.size() / targetValues.size();

        return (float) (1.0 - Math.round(Math.abs(sourceDistinctPercentage - targetDistinctPercentage) * 100.0) / 100.0);
    }
}
