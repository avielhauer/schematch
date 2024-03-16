package de.uni_marburg.schematch.matching.metadata;

import de.uni_marburg.schematch.data.Table;
import de.uni_marburg.schematch.matching.TablePairMatcher;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashMap;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ConstancyMatcher extends TablePairMatcher {

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

    private float calculateScore(List<String> source, List<String> target) {
        HashMap<String, Integer> source_map = new HashMap<>();
        HashMap<String, Integer> target_map = new HashMap<>();

        for (String entry : source) {
            source_map.computeIfPresent(
                    entry,
                    (key, count) -> count + 1
            );
            source_map.putIfAbsent(entry, 1);
        }

        for (String entry : target) {
            target_map.computeIfPresent(
                    entry,
                    (key, count) -> count + 1
            );
            target_map.putIfAbsent(entry, 1);
        }

        int maxSource = 0;
        int maxTarget = 0;

        for (Integer count : source_map.values()) {
            if (count > maxSource) maxSource = count;
        }

        for (Integer count : target_map.values()) {
            if (count > maxTarget) maxTarget = count;
        }

        //highest frequent value divided by number of rows
        float sourcePercentage = (float) maxSource / source.size();
        float targetPercentage = (float) maxTarget / target.size();

        //normalize to value between 0 and 1
        return Math.min(sourcePercentage, targetPercentage) / Math.max(sourcePercentage, targetPercentage);
    }

}
