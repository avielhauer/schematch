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

public class MeanLength extends TablePairMatcher{

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

        int i = 1;
        int j = 1;
        int SourceLength = 0;
        int TargetLength = 0;

        for (String s : sourceColumn) {
            if (s.isEmpty()){
                ++i;
                continue;
            }
            SourceLength = SourceLength + s.length();
            ++i;
        }
        int SourceMean = SourceLength / i;

        for (String t : targetColumn) {
            if (t.isEmpty()){
                ++j;
                continue;
            }
            TargetLength = TargetLength + t.length();
            ++j;
        }
        int TargetMean = TargetLength / j;

        if (TargetMean == 0) return 0.0f;

        if (TargetMean > SourceMean) {
            return (float) (SourceMean / TargetMean);
        }
        if (SourceMean == 0) return 0.0f;
        return (float) TargetMean / SourceMean;
    }
}


