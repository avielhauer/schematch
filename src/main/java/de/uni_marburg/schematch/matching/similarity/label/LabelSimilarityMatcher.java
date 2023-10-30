package de.uni_marburg.schematch.matching.similarity.label;

import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import de.uni_marburg.schematch.data.Table;
import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.similarity.SimilarityMeasure;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public abstract class LabelSimilarityMatcher extends Matcher {
    private SimilarityMeasure<String> similarityMeasure;

    public LabelSimilarityMatcher(SimilarityMeasure<String> similarityMeasure) {
        super();
        this.similarityMeasure = similarityMeasure;
    }

    @Override
    public float[][] match(TablePair tablePair) {
        Table sourceTable = tablePair.getSourceTable();
        Table targetTable = tablePair.getTargetTable();
        float[][] simMatrix = tablePair.getEmptySimMatrix();
        for (int i = 0; i < sourceTable.getNumberOfColumns(); i++) {
            for (int j = 0; j < targetTable.getNumberOfColumns(); j++) {
                simMatrix[i][j] = similarityMeasure.compare(sourceTable.getLabels().get(i), targetTable.getLabels().get(j));
            }
        }
        return simMatrix;
    }
}