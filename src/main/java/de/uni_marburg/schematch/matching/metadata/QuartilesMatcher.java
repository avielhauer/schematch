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
public class QuartilesMatcher extends TablePairMatcher {

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

                simMatrix[i][j] = calculateScore(sourceTable.getColumn(i), targetTable.getColumn(j));
            }
        }
        return simMatrix;
    }

    private float calculateScore(Column source, Column target) {
        List<Float> sourceFloat;
        if (source.getDatatype() == Datatype.INTEGER) {
            List<Integer> sourceInt = Datatype.castToInt(source);
            sourceFloat = new ArrayList<>();
            for (Integer s : sourceInt) {
                if (s == null) continue;
                sourceFloat.add((float) s);
            }
        } else {
            sourceFloat = Datatype.castToFloat(source);
        }

        List<Float> targetFloat;
        if (target.getDatatype() == Datatype.INTEGER) {
            List<Integer> targetInt = Datatype.castToInt(target);
            targetFloat = new ArrayList<>();
            for (Integer t : targetInt) {
                if (t == null) continue;
                targetFloat.add((float) t);
            }
        } else {
            targetFloat = Datatype.castToFloat(target);
        }

        //quartiles only matter if more than 4 data values are present
        if (sourceFloat.size() < 4) return 0.0f;
        if (targetFloat.size() < 4) return 0.0f;

        try {
            Collections.sort(sourceFloat);
            Collections.sort(targetFloat);
        } catch (NullPointerException ignore) {
            return 0.0f;
        }


        //(n + 1) * 0,25
        double sourceQ1 = calculateQuartile(sourceFloat, 0.25f);
        double sourceQ2 = calculateQuartile(sourceFloat, 0.5f);
        double sourceQ3 = calculateQuartile(sourceFloat, 0.75f);
        double targetQ1 = calculateQuartile(targetFloat, 0.25f);
        double targetQ2 = calculateQuartile(targetFloat, 0.5f);
        double targetQ3 = calculateQuartile(targetFloat, 0.75f);

        double q1;
        if (sourceQ1 == 0 || targetQ1 == 0) q1 = 0;
        else q1 = Math.min(sourceQ1, targetQ1) / Math.max(sourceQ1, targetQ1);

        double q2;
        if (sourceQ2 == 0 || targetQ2 == 0) q2 = 0;
        else q2 = Math.min(sourceQ2, targetQ2) / Math.max(sourceQ2, targetQ2);

        double q3;
        if (sourceQ3 == 0 || targetQ3 == 0) q3 = 0;
        else q3 = Math.min(sourceQ3, targetQ3) / Math.max(sourceQ3, targetQ3);

        return (float) (q1 + q2 + q3) / 3.0f;
    }

    private double calculateQuartile(List<Float> values, float p) {
        double pC = (values.size() + 1) * p;
        int upper = (int) Math.ceil(pC);
        int lower = (int) Math.floor(pC);
        return (values.get(upper - 1) + values.get(lower) - 1) / 2;
    }

}
