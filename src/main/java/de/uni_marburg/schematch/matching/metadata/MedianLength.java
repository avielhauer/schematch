package de.uni_marburg.schematch.matching.metadata;

import de.uni_marburg.schematch.data.Column;
import de.uni_marburg.schematch.data.Table;
import de.uni_marburg.schematch.data.metadata.Datatype;
import de.uni_marburg.schematch.matching.TablePairMatcher;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Arrays;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)

public class MedianLength extends TablePairMatcher{

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
                simMatrix[i][j] = calculateScore(sourceTable.getColumn(i).getValues(), targetTable.getColumn(j).getValues());
            }
        }
        return simMatrix;
    }

    private float calculateScore(List<String> sourceColumn, List<String> targetColumn) {

        int i = 0;
        int j = 0;
        if(sourceColumn.size() == 0) return 0.0f;
        if(targetColumn.size() == 0) return 0.0f;

        int sourceLengths[] = new int[sourceColumn.size()];
        int targetLengths[] = new int[targetColumn.size()];

        for (String s : sourceColumn) {
            if (s.isEmpty()){
                sourceLengths[i] = 0;
            }
            else{
                sourceLengths[i] = s.length();
            }
            ++i;
        }

        for (String t : targetColumn) {
            if (t.isEmpty()){
                targetLengths[j] = 0;
            }
            else{
                targetLengths[j] = t.length();
            }
            ++j;
        }

        float sourceMedian;
        float targetMedian;
        Arrays.sort(sourceLengths);
        Arrays.sort(targetLengths);

        if (sourceLengths.length % 2 == 0)
            sourceMedian = ((float)sourceLengths[sourceLengths.length/2] + (float)sourceLengths[sourceLengths.length/2 - 1])/2;
        else
            sourceMedian = (float) sourceLengths[sourceLengths.length/2];

        if (targetLengths.length % 2 == 0)
            targetMedian = ((float)targetLengths[targetLengths.length/2] + (float)targetLengths[targetLengths.length/2 - 1])/2;
        else
            targetMedian = (float) targetLengths[targetLengths.length/2];


        if (targetMedian == 0) return 0.0f;

        if (targetMedian > sourceMedian) {
            return (float) (sourceMedian / targetMedian);
        }
        if (sourceMedian == 0) return 0.0f;
        return (float) targetMedian / sourceMedian;
    }
}
