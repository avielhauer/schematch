package de.uni_marburg.schematch.matching.similarity.tokenizedlabel;

import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import de.uni_marburg.schematch.data.Table;
import de.uni_marburg.schematch.matching.TokenizedTablePairMatcher;
import de.uni_marburg.schematch.similarity.SimilarityMeasure;
import lombok.NoArgsConstructor;

import java.util.*;

@NoArgsConstructor
public abstract class TokenizedLabelSimilarityMatcher extends TokenizedTablePairMatcher {
    private SimilarityMeasure<Set<String>> similarityMeasure;

    public TokenizedLabelSimilarityMatcher(SimilarityMeasure<Set<String>> similarityMeasure) {
        super();
        this.similarityMeasure = similarityMeasure;
    }

    @Override
    public float[][] match(TablePair tablePair) {
        Table sourceTable = tablePair.getSourceTable();
        Table targetTable = tablePair.getTargetTable();
        float[][] simMatrix = tablePair.getEmptySimMatrix();
        for (int i = 0; i < sourceTable.getNumColumns(); i++) {
            Set<String> sourceTokens_i = sourceTable.getColumn(i).getLabelTokens(tokenizer);
            for (int j = 0; j < targetTable.getNumColumns(); j++) {
                Set<String> targetTokens_j = targetTable.getColumn(j).getLabelTokens(tokenizer);
                simMatrix[i][j] = similarityMeasure.compare(sourceTokens_i, targetTokens_j);
            }
        }
        return simMatrix;
    }
}