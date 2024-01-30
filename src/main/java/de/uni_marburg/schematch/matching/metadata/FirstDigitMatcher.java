package de.uni_marburg.schematch.matching.metadata;

import de.uni_marburg.schematch.data.Table;
import de.uni_marburg.schematch.data.metadata.Datatype;
import de.uni_marburg.schematch.matching.TablePairMatcher;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class FirstDigitMatcher extends TablePairMatcher {

    @Override
    public float[][] match(TablePair tablePair) {
        Table sourceTable = tablePair.getSourceTable();
        Table targetTable = tablePair.getTargetTable();
        float[][] simMatrix = tablePair.getEmptySimMatrix();
        for (int i = 0; i < sourceTable.getNumColumns(); i++) {
            Datatype sourceType = sourceTable.getColumn(i).getDatatype();
            for (int j = 0; j < targetTable.getNumColumns(); j++) {
                Datatype targetType = targetTable.getColumn(j).getDatatype();
                if (sourceType != Datatype.INTEGER && sourceType != Datatype.FLOAT) {
                    simMatrix[i][j] = 0.0f;
                    continue;
                }
                if (targetType != Datatype.INTEGER && targetType != Datatype.FLOAT) {
                    simMatrix[i][j] = 0.0f;
                    continue;
                }
                simMatrix[i][j] = calculateScore(sourceTable.getColumn(i).getValues(), targetTable.getColumn(j).getValues());
            }
        }
        return simMatrix;
    }

    private float calculateScore(List<String> sourceColumn, List<String> targetColumn) {
        HashMap<Integer, Integer> sourceDistribution = new HashMap<>();
        HashMap<Integer, Integer> targetDistribution = new HashMap<>();



        //count Benford's distribution
        for (int i = 0; i <= 9; i++) {
            sourceDistribution.put(i, 0);
            targetDistribution.put(i, 0);
        }

        getDistributions(sourceColumn, sourceDistribution);

        getDistributions(targetColumn, targetDistribution);

        //calculate similarity of first digit frequency
        int sourceElements = sourceColumn.size();
        int targetElements = targetColumn.size();
        List<Float> similarities = new ArrayList<>();
        for (int i = 0; i <= 9; i++) {
            float sourcePercentage = (float) sourceDistribution.get(i) / sourceElements;
            float targetPercentage = (float) targetDistribution.get(i) / targetElements;

            float similarity = (float) (Math.round(Math.abs(sourcePercentage - targetPercentage) * 100.0) / 100.0);
            similarities.add(similarity);
        }

        //now average the similarities - Σ(similarities) / 10
        return (float) (similarities.stream().reduce(0.0f, Float::sum) / 10.0);
    }

    private void getDistributions(List<String> targetColumn, HashMap<Integer, Integer> targetDistribution) {
        for (String entry : targetColumn) {
            try {
                Integer.parseInt(entry);
                int index = 0;
                if (entry.charAt(0) == '-') index = 1;
                Integer firstDigit = Integer.parseInt(String.valueOf(entry.charAt(index)));
                targetDistribution.computeIfPresent(firstDigit, (key, count) -> count + 1);
            } catch (NumberFormatException ignored) {
            }
        }
    }

}
