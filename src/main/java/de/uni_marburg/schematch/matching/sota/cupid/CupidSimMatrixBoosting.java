package de.uni_marburg.schematch.matching.sota.cupid;

import de.uni_marburg.schematch.boosting.SimMatrixBoosting;
import de.uni_marburg.schematch.matchtask.MatchTask;
import de.uni_marburg.schematch.matchtask.matchstep.SimMatrixBoostingStep;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import de.uni_marburg.schematch.utils.ArrayUtils;
import lombok.Setter;

import java.io.IOException;
import java.util.*;

import static de.uni_marburg.schematch.matching.sota.cupid.TreeMatch.convertSimMatrix;

@Setter
public class CupidSimMatrixBoosting implements SimMatrixBoosting {
    /**
     * Threshold for the mapping and for strong links in the structural similarity
     */
    private float th_accept = 0.7f;
    /**
     * wsim weight for leaves
     */
    private float leaf_w_struct = 0.2f;
    /**
     * wsim weights for non-leave nodes
     */
    private float w_struct = 0.2f;
    /**
     * Threshold for the non-leaf links, if wsim is above th_high, the nodes leaves links ssim will be
     * increased by c_inc
     */
    private float th_high = 0.6f;
    /**
     * Threshold for the non-leaf links, if wsim is under th_low, the nodes leaves links ssim will be
     * decreased by d_dex
     */
    private float th_low = 0.35f;
    /**
     * Value by which the leaves ssim will be increased
     */
    private float c_inc = 1.2f;
    /**
     * Value by which the leaves ssim will be decreased
     */
    private float c_dec = 0.9f;
    /**
     * When true, the algorithm will run the simple data type detection of Lars and Leif and will map the data type to
     * the closest Cupid data type.
     * When false, the algorithm will run an extended data type detection based on the implementation of Lars and Leif,
     * also considering table values.
     */
    private Boolean use_simple_data_types = false;
    /**
     * Boolean to decide, if the algorithm should recalculate the wsim, while mapping
     */
    private Boolean wsrecalc = true;
    /**
     * Whether the algorithm should map, meaning cutting of values under th_acceot
     */
    private Boolean mapping = true;

    private WordNetFunctionalities wnf;
    private LinguisticMatching linguisticMatching;
    private Map<Integer, Pair<Set<String>, SchemaTree>> trees = new HashMap<>();

    @Override
    public String toString() {
        return "CupidSimMatrixBoosting(" +
                "thaccept=" + th_accept +
                "/mapping=" + mapping +
                "/leafWStruct=" + leaf_w_struct +
                "/wStruct=" + w_struct +
                "/useSimpleDataTypes" + use_simple_data_types +
                "/wsrecalc=" + wsrecalc +
                "/thHigh=" + th_high +
                "/thLow=" + th_low +
                "/cInc=" + c_inc +
                "/cDec=" + c_dec +
                ")";
    }

    public CupidSimMatrixBoosting() {
        WordNetFunctionalities wnf;
        try {
            this.wnf = new WordNetFunctionalities();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.linguisticMatching = new LinguisticMatching(this.wnf);
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

        List<TablePair> tablePairs = matchTask.getTablePairs();

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

            Set<String> categories = new HashSet<String>();
            categories.addAll(sourceTree.getFirst());
            categories.addAll(targetTree.getFirst());

            Map<String, Map<StringPair, Float>> sims = new TreeMatch().treeMatch(
                    sourceTree.getSecond(),
                    targetTree.getSecond(),
                    convertSimMatrix(tablePair, simMatrix),
                    categories,
                    leaf_w_struct,
                    w_struct,
                    th_accept,
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
                    w_struct,
                    th_accept
            );


            int sourceTableOffset = tablePair.getSourceTable().getOffset();
            int targetTableOffset = tablePair.getTargetTable().getOffset();

            float[][] subSimMatrix;
            if (wsrecalc) {
                subSimMatrix = CupidMatcher.mapSimilarityMatrix(tablePair, newSims, th_accept, leaf_w_struct, mapping);
            } else {
                subSimMatrix = CupidMatcher.mapSimilarityMatrix(tablePair, newSims.get("wsim"), th_accept, mapping);
            }

            ArrayUtils.insertSubmatrixInMatrix(subSimMatrix, simMatrix, sourceTableOffset, targetTableOffset);
        }

        return simMatrix;
    }
}
