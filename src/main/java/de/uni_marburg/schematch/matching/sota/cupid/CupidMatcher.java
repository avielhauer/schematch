package de.uni_marburg.schematch.matching.sota.cupid;

import de.uni_marburg.schematch.data.Column;
import de.uni_marburg.schematch.data.Table;
import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matchtask.MatchTask;
import de.uni_marburg.schematch.matchtask.matchstep.MatchingStep;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import de.uni_marburg.schematch.utils.ArrayUtils;
import lombok.Getter;

import java.io.IOException;
import java.util.*;


/**
 * TODO: Implement Cupid Matcher
 */
public class CupidMatcher extends Matcher {

    private final Settings settings;

    /**
     * Initializes default setting Cupid Matcher object with th_accept = 0.7, leaf_w_struct = 0.2, w_struct 0.2 and
     * use_simple_data_types = flase
     */
    public CupidMatcher() {
        this.settings = new Settings(.7f,.2f,.2f,false);
    }

    /**
     * Initializes Cupid Matcher object with custom th_accept and use_simple_data_types and default leaf_w_struct = 0.2
     * and w_struct = 0.2
     * @param th_accept Threshold for mapping and strong links in the structural matching
     * @param use_simple_data_types use simple data types (boolean)
     */
    public CupidMatcher(float th_accept, boolean use_simple_data_types) {
        this.settings = new Settings(th_accept,.2f,.2f,use_simple_data_types);
    }

    /**
     * Initializes Cupid Matcher object with custom th_accept and default leaf_w_struct = 0.2, w_struct = 0.2 and
     * use_simple_data_types = false
     * @param th_accept Threshold for mapping and strong links in the structural matching
     */
    public CupidMatcher(float th_accept) {
        this.settings = new Settings(th_accept,.2f,.2f,false);
    }

    /**
     * Initializes Cupid Matcher object custom settings
     * @param th_accept Threshold for mapping and strong links in the structural matching
     * @param leaf_w_struct Weight of the structural similarity, to calculate the weighted similarity between structural
     *                    similarity and linguistic similarity of the leaf node pairs
     * @param w_struct Weight of the structural similarity, to calculate the weighted similarity between structural
     *                similarity and linguistic similarity of the non-leaf node pairs
     * @param use_simple_data_types use simple data types (boolean)
     */
    public CupidMatcher(float th_accept, float leaf_w_struct, float w_struct, boolean use_simple_data_types) {
        this.settings = new Settings(th_accept,leaf_w_struct,w_struct,use_simple_data_types);
    }

    /**
     * Starts cupid matching
     * @param matchTask MatchTask to match
     * @param matchStep Current MatchStep (MatchingStep)
     * @return Similarity matrix for the given match task. Position (i,j) represents the similarity score for
     * the column pair (i-th source column, j-th target column)
     */
    @Override
    public float[][] match(MatchTask matchTask, MatchingStep matchStep) {
        float th_high = 0.6f;
        float th_low = 0.35f;
        float c_inc = 1.2f;
        float c_dec = 0.9f;
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
            Pair<Set<String>,SchemaTree> sourceTree = get(trees, tablePair.getSourceTable());
            Pair<Set<String>,SchemaTree> targetTree = get(trees, tablePair.getTargetTable());
            if (sourceTree == null) {
                sourceTree = new TreeBuilder().buildTreeFromTable(tablePair.getSourceTable(), settings.use_simple_data_types);
                trees.add(sourceTree);
            }
            if (targetTree == null) {
                targetTree = new TreeBuilder().buildTreeFromTable(tablePair.getTargetTable(), settings.use_simple_data_types);
                trees.add(targetTree);
            }
            Set<String> categories = new HashSet<>();
            categories.addAll(sourceTree.getFirst());
            categories.addAll(targetTree.getFirst());

            Map<String, Map<StringPair, Float>> sims = new TreeMatch().treeMatch(
                    sourceTree.getSecond(),
                    targetTree.getSecond(),
                    categories,
                    settings.leaf_w_struct,
                    settings.w_struct,
                    settings.th_accept,
                    th_high,
                    th_low,
                    c_inc,
                    c_dec,
                    th_ns,
                    parallelism,
                    linguisticMatching
            );

            Map<String, Map<StringPair, Float>> newSims = new TreeMatch().recomputewsim(
                    sourceTree.getSecond(),
                    targetTree.getSecond(),
                    sims,
                    settings.w_struct,
                    settings.th_accept,
                    linguisticMatching
            );


            int sourceTableOffset = tablePair.getSourceTable().getOffset();
            int targetTableOffset = tablePair.getTargetTable().getOffset();
            ArrayUtils.insertSubmatrixInMatrix(mapSimilarityMatrix(tablePair, newSims.get("wsim"), settings.th_accept), simMatrix, sourceTableOffset, targetTableOffset);
        }

        return simMatrix;
    }

    /**
     * Method to search for Schema tree and its categories in the trees
     * @param trees list of schema trees
     * @param table Table of which the schema tree should be loaded
     * @return schema tree and categories, if they exist, else null
     */
    static Pair<Set<String>, SchemaTree> get(List<Pair<Set<String>, SchemaTree>> trees, Table table) {
        for (Pair<Set<String>, SchemaTree> pair: trees) {
            if (pair.getSecond().getHashCode() == table.hashCode()) {
                return pair;
            }
        }
        return null;
    }

    /**
     * Maps similarity with a value higher than th_Accept to a similarity matrix
     * @param tablePair table pair of the corresponding tables
     * @param sims similarities
     * @param th_accept threshold
     * @return Similarity matrix for the given match task. Position (i,j) represents the similarity score for
     * the column pair (i-th source column, j-th target column)
     */
    public static float[][] mapSimilarityMatrix(TablePair tablePair, Map<StringPair, Float> sims, float th_accept) {
        float[][] symMatrix = tablePair.getEmptySimMatrix();

        List<Column> sourceColumns = tablePair.getSourceTable().getColumns();
        List<Column> targetColumns = tablePair.getTargetTable().getColumns();

        for (int i = 0; i < sourceColumns.size(); i++) {
            for (int j = 0; j < targetColumns.size(); j++) {
                StringPair p = new StringPair(sourceColumns.get(i).getLabel(), targetColumns.get(j).getLabel());
                float wsim = 0;
                if(sims.containsKey(p)) {
                    wsim = sims.get(p);
                }
                if (wsim >= th_accept) symMatrix[i][j] = wsim;
            }
        }

        return symMatrix;
    }

    @Getter
    private record Settings(float th_accept, float leaf_w_struct, float w_struct, boolean use_simple_data_types) {
        @Override
        public String toString() {
            return "Settings=" +
                    "th_accept=" + this.th_accept +
                    "\\leaf_w_struct=" + this.leaf_w_struct +
                    "\\w_struct=" + this.w_struct +
                    "\\use_simple_data_types=" + this.use_simple_data_types + ")";
        }
    }
}