package de.uni_marburg.schematch.matching.sota.cupid;

import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matchtask.MatchTask;
import de.uni_marburg.schematch.matchtask.matchstep.MatchingStep;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import de.uni_marburg.schematch.utils.ArrayUtils;
import lombok.Setter;

import java.io.IOException;
import java.util.*;

@Setter
public class CupidMatcherLinguistic extends Matcher {
    /**
     * Threshold for the mapping and for strong links in the structural similarity
     */
    private float th_accept = 0.7f;
    /**
     * Threshold to filter out not matching data types in the linguistic matching
     */
    private float th_ns = 0.7f;
    /**
     * Number of threads used in the linguistic matching
     */
    private int parallelism = 1;

    /**
     * When true, the algorithm will run the simple data type detection of Lars and Leif and will map the data type to
     * the closest Cupid data type.
     * When false, the algorithm will run an extended data type detection based on the implementation of Lars and Leif,
     * also considering table values.
     */
    private Boolean use_simple_data_types = false;
    /**
     * Whether the algorithm should map, meaning cutting of values under th_acceot
     */
    private Boolean mapping = true;

    private Map<Integer, Pair<Set<String>, SchemaTree>> trees = new HashMap<>();

    private WordNetFunctionalities wnf;

    private LinguisticMatching linguisticMatching;

    public CupidMatcherLinguistic() {
        try {
            this.wnf = new WordNetFunctionalities();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.linguisticMatching = new LinguisticMatching(this.wnf);
    }

    /**
     * Custom toString method
     * @return CupidMatcherLinguistic settings represented as string
     */
    @Override
    public String toString() {
        return "CupidMatcherLinguistic(" +
                "thaccept=" + th_accept +
                "_mapping=" + mapping +
                "_useSimpleDataTypes" + use_simple_data_types +
                "_thns=" + th_ns +
                "_parallelism=" + parallelism +
                ")";
    }



    /**
     * @param matchTask MatchTask to match
     * @param matchStep Current MatchStep (MatchingStep)
     * @return Similarity matrix for the given match task. Position (i,j) represents the similarity score for
     * the column pair (i-th source column, j-th target column)
     */
    @Override
    public float[][] match(MatchTask matchTask, MatchingStep matchStep) {

        List<TablePair> tablePairs = matchTask.getTablePairs();

        float[][] simMatrix = matchTask.getEmptySimMatrix();

        for (TablePair tablePair : tablePairs) {
            Pair<Set<String>,SchemaTree> sourceTree;
            int sHash = tablePair.getSourceTable().hashCode();
            Pair<Set<String>,SchemaTree> targetTree;
            int tHash = tablePair.getTargetTable().hashCode();

            if (trees.containsKey(sHash)) {
                sourceTree = trees.get(sHash);
            } else {
                sourceTree = new TreeBuilder().buildTreeFromTable(tablePair.getSourceTable(), use_simple_data_types);
                trees.put(sHash,sourceTree);
            }
            if (trees.containsKey(tHash)) {
                targetTree = trees.get(tHash);
            } else {
                targetTree = new TreeBuilder().buildTreeFromTable(tablePair.getTargetTable(), use_simple_data_types);
                trees.put(tHash,targetTree);
            }

            Set<String> categories = new HashSet<>();
            categories.addAll(sourceTree.getFirst());
            categories.addAll(targetTree.getFirst());

            Map<String, Map<String, Double>> compatibilityTable = linguisticMatching.computeCompatibility(categories);

            Map<StringPair, Float> lSims = linguisticMatching.comparison(sourceTree.getSecond(), targetTree.getSecond(), compatibilityTable, th_ns, parallelism);


            int sourceTableOffset = tablePair.getSourceTable().getOffset();
            int targetTableOffset = tablePair.getTargetTable().getOffset();
            ArrayUtils.insertSubmatrixInMatrix(CupidMatcher.mapSimilarityMatrix(tablePair, lSims, th_accept, mapping), simMatrix, sourceTableOffset, targetTableOffset);
        }

        return simMatrix;
    }
}