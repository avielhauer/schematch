package de.uni_marburg.schematch.matching.sota.cupid;

import de.uni_marburg.schematch.data.Table;
import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matchtask.MatchTask;
import de.uni_marburg.schematch.matchtask.matchstep.MatchingStep;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import de.uni_marburg.schematch.utils.ArrayUtils;

import java.io.IOException;
import java.util.*;

/**
 * TODO: Implement Cupid Matcher
 */
public class CupidMatcherLinguistic extends Matcher {

    private boolean use_simple_data_types = false;

    /**
     * Initializes CupidMatcherLinguistic object with use_simple_data_types = false
     */
    public CupidMatcherLinguistic() {

    }

    /**
     * Initializes CupidMatcherLinguistic object with custom use_simple_data_types (java)
     * @param use_simple_data_types use simple data types (boolean)
     */
    public CupidMatcherLinguistic(boolean use_simple_data_types) {
        this.use_simple_data_types = use_simple_data_types;
    }

    /**
     * @param matchTask MatchTask to match
     * @param matchStep Current MatchStep (MatchingStep)
     * @return Similarity matrix for the given match task. Position (i,j) represents the similarity score for
     * the column pair (i-th source column, j-th target column)
     */
    @Override
    public float[][] match(MatchTask matchTask, MatchingStep matchStep) {
        float th_ns = 0.7f;
        int parallelism = 1;
        WordNetFunctionalities wnf;
        try {
            wnf = new WordNetFunctionalities();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        LinguisticMatching linguisticMatching = new LinguisticMatching(wnf);

        List<TablePair> tablePairs = matchTask.getTablePairs();

        float[][] simMatrix = matchTask.getEmptySimMatrix();
        List<Pair<Set<String>,SchemaTree>> trees = new ArrayList<>();

        for (TablePair tablePair : tablePairs) {
            Pair<Set<String>,SchemaTree> sourceTree = CupidMatcher.get(trees, tablePair.getSourceTable());
            Pair<Set<String>,SchemaTree> targetTree = CupidMatcher.get(trees, tablePair.getTargetTable());
            if (sourceTree == null) {
                sourceTree = new TreeBuilder().buildTreeFromTable(tablePair.getSourceTable(), use_simple_data_types);
                trees.add(sourceTree);
            }
            if (targetTree == null) {
                targetTree = new TreeBuilder().buildTreeFromTable(tablePair.getTargetTable(), use_simple_data_types);
                trees.add(targetTree);
            }
            Set<String> categories = new HashSet<String>();
            categories.addAll(sourceTree.getFirst());
            categories.addAll(targetTree.getFirst());

            Map<String, Map<String, Double>> compatibilityTable = linguisticMatching.computeCompatibility(categories);

            Map<StringPair, Float> lSims = linguisticMatching.comparison(sourceTree.getSecond(), targetTree.getSecond(), compatibilityTable, th_ns, parallelism);


            int sourceTableOffset = tablePair.getSourceTable().getOffset();
            int targetTableOffset = tablePair.getTargetTable().getOffset();
            ArrayUtils.insertSubmatrixInMatrix(CupidMatcher.mapSimilarityMatrix(tablePair, lSims, 0.0f), simMatrix, sourceTableOffset, targetTableOffset);
        }

        return simMatrix;
    }
}