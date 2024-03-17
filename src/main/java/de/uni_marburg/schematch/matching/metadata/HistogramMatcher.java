package de.uni_marburg.schematch.matching.metadata;

import de.uni_marburg.schematch.data.Column;
import de.uni_marburg.schematch.data.Table;
import de.uni_marburg.schematch.data.metadata.Datatype;
import de.uni_marburg.schematch.matching.TablePairMatcher;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.*;

@Data
@EqualsAndHashCode(callSuper = true)
public class HistogramMatcher extends TablePairMatcher {

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
                if (sourceType != targetType) {
                    simMatrix[i][j] = 0f;
                    continue;
                }
                simMatrix[i][j] = calculateScore(sourceColumn, targetColumn);
            }
        }
        return simMatrix;
    }


    private float calculateScore(Column sourceColumn, Column targetColumn) {
        List<String> sourceList = sourceColumn.getValues();
        List<String> targetList = targetColumn.getValues();

        HashMap<String, Integer> sourceMap = new HashMap<>();
        HashMap<String, Integer> targetMap = new HashMap<>();

        for (String item : sourceList) {
            sourceMap.putIfAbsent(item, 0);
            sourceMap.put(item, sourceMap.get(item) + 1);
        }

        for (String item : targetList) {
            targetMap.putIfAbsent(item, 0);
            targetMap.put(item, targetMap.get(item) + 1);
        }

        Set<String> bins = new HashSet<>(sourceMap.keySet());
        bins.addAll(targetMap.keySet());

        ArrayList<String> binsList = new ArrayList<>(bins);
        Collections.sort(binsList);

        double[] source = new double[bins.size()];
        double[] target = new double[bins.size()];

        for (int i = 0; i < binsList.size(); i++) {
            String currKey = binsList.get(i);
            source[i] = sourceMap.getOrDefault(currKey, 0);
            target[i] = targetMap.getOrDefault(currKey, 0);
        }

        double emd = EMD(source, target);
        return (float) emd;
    }


    private double EMD(double[] source, double[] target) {
        double lastDistance = 0;
        double totalDistance = 0;
        for (int i = 0; i < source.length; i++) {
            final double currentDistance = source[i] + lastDistance - target[i];
            totalDistance += Math.abs(currentDistance);
            lastDistance = currentDistance;
        }
        return totalDistance;
    }

}
