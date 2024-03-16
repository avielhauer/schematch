package de.uni_marburg.schematch.matching.sota.cupid;

import de.uni_marburg.schematch.boosting.SimMatrixBoosting;
import de.uni_marburg.schematch.matchtask.MatchTask;
import de.uni_marburg.schematch.matchtask.matchstep.SimMatrixBoostingStep;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import de.uni_marburg.schematch.utils.ArrayUtils;
import lombok.Getter;

import java.io.IOException;
import java.util.*;

import static de.uni_marburg.schematch.matching.sota.cupid.TreeMatch.convertSimMatrix;

public class CupidSimMatrixBoosting implements SimMatrixBoosting {

    private final Settings settings;

    /**
     * Initializes CupidSimMatrixBoosting object with default settings th_accept = 0.7, leaf_w_struct = 0.2, w_struct = 0.2 and
     * use_simple_data_types = false
     */
    public CupidSimMatrixBoosting() {
        this.settings = new Settings(.7f,.2f,.2f,false);
    }

    /**
     * Initializes CupidSimMatrixBoosting object with custom th_accept and use_simple_data_types and default leaf_w_struct = 0.2
     * and w_struct = 0.2
     * @param th_accept Threshold for mapping and strong links in the structural matching
     * @param use_simple_data_types use simple data types (boolean)
     */
    public CupidSimMatrixBoosting(float th_accept, boolean use_simple_data_types) {
        this.settings = new Settings(th_accept,.2f,.2f,use_simple_data_types);
    }

    /**
     * Initializes CupidSimMatrixBoosting object with custom th_accept and default leaf_w_struct = 0.2, w_struct = 0.2 and
     * use_simple_data_types = false
     * @param th_accept Threshold for mapping and strong links in the structural matching
     */
    public CupidSimMatrixBoosting(float th_accept) {
        this.settings = new Settings(th_accept,.2f,.2f,false);
    }

    /**
     * Initializes CupidSimMatrixBoosting object custom settings
     * @param th_accept Threshold for mapping and strong links in the structural matching
     * @param leaf_w_struct Weight of the structural similarity, to calculate the weighted similarity between structural
     *                    similarity and linguistic similarity of the leaf node pairs
     * @param w_struct Weight of the structural similarity, to calculate the weighted similarity between structural
     *                similarity and linguistic similarity of the non-leaf node pairs
     * @param use_simple_data_types use simple data types (boolean)
     */
    public CupidSimMatrixBoosting(float th_accept, float leaf_w_struct, float w_struct, boolean use_simple_data_types) {
        this.settings = new Settings(th_accept,leaf_w_struct,w_struct,use_simple_data_types);
    }

    /**
     * Starts cupid sim matrix boosting
     * @param matchTask MatchTask to match
     * @param matchStep Current MatchStep (MatchingStep)
     * @return Similarity matrix for the given match task. Position (i,j) represents the similarity score for
     * the column pair (i-th source column, j-th target column)
     */
    @Override
    public float[][] run(MatchTask matchTask, SimMatrixBoostingStep matchStep, float[][] simMatrix) {
        float th_high = 0.6f;
        float th_low = 0.35f;
        float c_inc = 1.2f;
        float c_dec = 0.9f;

        List<TablePair> tablePairs = matchTask.getTablePairs();

        List<Pair<Set<String>,SchemaTree>> trees = new ArrayList<>();
        WordNetFunctionalities wnf;
        try {
            wnf = new WordNetFunctionalities();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        LinguisticMatching linguisticMatching = new LinguisticMatching(wnf);

        for (TablePair tablePair : tablePairs) {
            Pair<Set<String>,SchemaTree> sourceTree = CupidMatcher.get(trees, tablePair.getSourceTable());
            Pair<Set<String>,SchemaTree> targetTree = CupidMatcher.get(trees, tablePair.getTargetTable());
            if (sourceTree == null) {
                sourceTree = new TreeBuilder().buildTreeFromTable(tablePair.getSourceTable(), settings.use_simple_data_types);
                trees.add(sourceTree);
            }
            if (targetTree == null) {
                targetTree = new TreeBuilder().buildTreeFromTable(tablePair.getTargetTable(), settings.use_simple_data_types);
                trees.add(targetTree);
            }
            Set<String> categories = new HashSet<String>();
            categories.addAll(sourceTree.getFirst());
            categories.addAll(targetTree.getFirst());

            Map<String, Map<StringPair, Float>> sims = new TreeMatch().treeMatch(
                    sourceTree.getSecond(),
                    targetTree.getSecond(),
                    convertSimMatrix(tablePair, simMatrix),
                    categories,
                    settings.leaf_w_struct,
                    settings.w_struct,
                    settings.th_accept,
                    th_high,
                    th_low,
                    c_inc,
                    c_dec,
                    linguisticMatching
            );

            Map<String, Map<StringPair, Float>> newSims = new TreeMatch().recomputewsim(
                    sourceTree.getSecond(),
                    targetTree.getSecond(),
                    sims,
                    settings.w_struct,
                    settings.th_accept
            );


            int sourceTableOffset = tablePair.getSourceTable().getOffset();
            int targetTableOffset = tablePair.getTargetTable().getOffset();
            ArrayUtils.insertSubmatrixInMatrix(CupidMatcher.mapSimilarityMatrix(tablePair, newSims.get("wsim"), settings.th_accept), simMatrix, sourceTableOffset, targetTableOffset);
        }

        return simMatrix;
    }

    @Getter
    private record Settings(float th_accept, float leaf_w_struct, float w_struct, boolean use_simple_data_types) {}
}
