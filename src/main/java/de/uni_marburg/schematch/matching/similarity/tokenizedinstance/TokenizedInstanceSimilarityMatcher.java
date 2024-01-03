package de.uni_marburg.schematch.matching.similarity.tokenizedinstance;

import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import de.uni_marburg.schematch.data.*;
import de.uni_marburg.schematch.matching.TokenizedTablePairMatcher;
import de.uni_marburg.schematch.similarity.SimilarityMeasure;
import lombok.NoArgsConstructor;

import java.util.*;

@NoArgsConstructor
public abstract class TokenizedInstanceSimilarityMatcher extends TokenizedTablePairMatcher {
    private SimilarityMeasure<Set<String>> similarityMeasure;

    public TokenizedInstanceSimilarityMatcher(SimilarityMeasure<Set<String>> similarityMeasure) {
        super();
        this.similarityMeasure = similarityMeasure;
    }

    @Override
    public float[][] match(TablePair tablePair) {
        Table sourceTable = tablePair.getSourceTable();
        Table targetTable = tablePair.getTargetTable();
        float[][] simMatrix = tablePair.getEmptySimMatrix();
        for (int i = 0; i < sourceTable.getNumColumns(); i++) {
            Set<String> sourceTokens_i = new HashSet<>(sourceTable.getColumn(i).getValuesTokens(tokenizer));
            for (int j = 0; j < targetTable.getNumColumns(); j++) {
                Set<String> targetTokens_j = new HashSet<>(targetTable.getColumn(j).getValuesTokens(tokenizer));
                simMatrix[i][j] = similarityMeasure.compare(sourceTokens_i, targetTokens_j);
            }
        }
        return simMatrix;
    }
}