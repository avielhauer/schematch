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
public class MinimumMatcher extends TablePairMatcher {

    @Override
    public float[][] match(TablePair tablePair) {

        Table sourceTable = tablePair.getSourceTable();
        Table targetTable = tablePair.getTargetTable();

        float[][] simMatrix = tablePair.getEmptySimMatrix();
        for (int i = 0; i < sourceTable.getNumColumns(); i++) {
            Datatype datatype_i = sourceTable.getColumn(i).getDatatype();
            for (int j = 0; j < targetTable.getNumColumns(); j++) {
                Datatype datatype_j = targetTable.getColumn(j).getDatatype();

                if (datatype_i != datatype_j) {
                    simMatrix[i][j] = 0;
                    continue;
                }

                Column sourceColumn = sourceTable.getColumn(i);
                Column targetColumn = targetTable.getColumn(j);
                switch (datatype_i) {
                    case INTEGER -> simMatrix[i][j] = integerExtrema(sourceColumn, targetColumn);
                    case FLOAT -> simMatrix[i][j] = floatExtrema(sourceColumn, targetColumn);
                    default -> simMatrix[i][j] = 0;
                }
            }
        }

        return simMatrix;
    }

    private float integerExtrema(Column sourceRaw, Column targetRaw) {
        int sourceMin = Integer.MAX_VALUE;
        int targetMin = Integer.MAX_VALUE;

        List<Integer> source = Datatype.castToInt(sourceRaw);
        List<Integer> target = Datatype.castToInt(targetRaw);

        for (Integer s : source) {
            if (s == null) {
                continue;
            }
            if (s < sourceMin) sourceMin = s;
        }

        for (Integer t : target) {
            if (t == null) {
                continue;
            }
            if (t < targetMin) targetMin = t;
        }

        return (float) Math.min(sourceMin, targetMin) / Math.max(sourceMin, targetMin);
    }

    private float floatExtrema(Column sourceRaw, Column targetRaw) {
        float sourceMin = Float.MAX_VALUE;
        float targetMin = Float.MAX_VALUE;

        List<Float> source = Datatype.castToFloat(sourceRaw);
        List<Float> target = Datatype.castToFloat(targetRaw);

        for (Float s : source) {
            if (s == null) {
                continue;
            }
            if (s < sourceMin) sourceMin = s;
        }

        for (Float t : target) {
            if (t == null) {
                continue;
            }
            if (t < targetMin) targetMin = t;
        }

        return Math.min(sourceMin, targetMin) / Math.max(sourceMin, targetMin);
    }

}
