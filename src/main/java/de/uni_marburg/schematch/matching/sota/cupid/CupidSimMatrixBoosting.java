package de.uni_marburg.schematch.matching.sota.cupid;

import de.uni_marburg.schematch.boosting.SimMatrixBoosting;
import de.uni_marburg.schematch.data.Column;
import de.uni_marburg.schematch.matchtask.MatchTask;
import de.uni_marburg.schematch.matchtask.matchstep.SimMatrixBoostingStep;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;
import de.uni_marburg.schematch.utils.ArrayUtils;
import org.apache.lucene.document.IntRange;

import java.io.IOException;
import java.util.*;

public class CupidSimMatrixBoosting implements SimMatrixBoosting {
    @Override
    public float[][] run(MatchTask matchTask, SimMatrixBoostingStep matchStep, float[][] simMatrix) {
        float leaf_w_struct = 0.2f;
        float w_struct = 0.2f;
        float th_accept = 0.7f;
        float th_high = 0.6f;
        float th_low = 0.35f;
        float c_inc = 1.2f;
        float c_dec = 0.9f;

        List<TablePair> tablePairs = matchTask.getTablePairs();

        List<Pair<Set<String>,SchemaTree>> trees = new ArrayList<>();
        WordNetFunctionalities wnf = null;
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
                sourceTree = CupidMatcher.buildTreeFromTable(tablePair.getSourceTable());
                trees.add(sourceTree);
            }
            if (targetTree == null) {
                targetTree = CupidMatcher.buildTreeFromTable(tablePair.getTargetTable());
                trees.add(targetTree);
            }
            Set<String> categories = new HashSet<String>();
            categories.addAll(sourceTree.getFirst());
            categories.addAll(targetTree.getFirst());

            Map<String, Map<StringPair, Float>> sims = treeMatch(
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

            Map<String, Map<StringPair, Float>> newSims = recomputewsim(
                    sourceTree.getSecond(),
                    targetTree.getSecond(),
                    sims,
                    w_struct,
                    th_accept
            );


            int sourceTableOffset = tablePair.getSourceTable().getOffset();
            int targetTableOffset = tablePair.getTargetTable().getOffset();
            ArrayUtils.insertSubmatrixInMatrix(mapSimilarityMatrix(tablePair, newSims, th_accept), simMatrix, sourceTableOffset, targetTableOffset);
        }

        return simMatrix;
    }

    private float[][] mapSimilarityMatrix(TablePair tablePair, Map<String, Map<StringPair, Float>> sims, float th_accept) {
        float[][] symMatrix = tablePair.getEmptySimMatrix();

        List<Column> sourceColumns = tablePair.getSourceTable().getColumns();
        List<Column> targetColumns = tablePair.getTargetTable().getColumns();

        Map<StringPair, Float> wsims = sims.get("wsim");

        for (int i = 0; i < sourceColumns.size(); i++) {
            for (int j = 0; j < targetColumns.size(); j++) {
                StringPair p = new StringPair(sourceColumns.get(i).getLabel(), targetColumns.get(j).getLabel());
                float wsim = wsims.get(p);
                if (wsim >= th_accept) symMatrix[i][j] = wsim;
            }
        }

        return symMatrix;
    }

    private Map<StringPair, Float> convertSimMatrix(TablePair tablePair, float[][] simMatrix) {
        int sourceTableOffset = tablePair.getSourceTable().getOffset();
        int targetTableOffset = tablePair.getTargetTable().getOffset();

        Map<StringPair, Float> sims = new HashMap<>();

        for (int x = 0; x < tablePair.getSourceTable().getNumColumns(); x++) {
            for (int y = 0; y < tablePair.getTargetTable().getNumColumns(); y++) {
                sims.put(
                        new StringPair(
                                tablePair.getSourceTable().getColumn(x).getLabel(),
                                tablePair.getTargetTable().getColumn(y).getLabel()
                        ),
                        simMatrix[x+sourceTableOffset][y+targetTableOffset]
                );
            }
        }
        return sims;
    }

    private Map<String, Map<StringPair, Float>> treeMatch(
            SchemaTree sourceTree,
            SchemaTree targetTree,
            Map<StringPair, Float> lSims,
            Set<String> categories,
            float leafWStruct,
            float wStruct,
            float thAccept,
            float thHigh,
            float thLow,
            float cInc,
            float cDec,
            LinguisticMatching linguisticMatching
    ) {
        Map<String, Map<String, Double>> compatibilityTable = linguisticMatching.computeCompatibility(categories);
        List<SchemaElementNode> sLeaves = sourceTree.getLeaves();
        List<SchemaElementNode> tLeaves = targetTree.getLeaves();
        Map<String, Map<StringPair, Float>> sims = getSims(sLeaves, tLeaves, compatibilityTable, lSims, leafWStruct);
        List<SchemaElementNode> sPostOrder = sourceTree.postOrder();
        List<SchemaElementNode> tPostOrder = targetTree.postOrder();

        for (SchemaElementNode s : sPostOrder) {
            for (SchemaElementNode t : tPostOrder) {
                StringPair pair = new StringPair(s.name, t.name);
                if (s.height() == t.height()) {
                    float ssim = StructuralSimilarity.computeSSim(s, t, sims, thAccept);
                    if (Float.isNaN(ssim)) {
                        continue;
                    }
                    if (!lSims.containsKey(pair)) {
                        sims.get("lsim").put(pair, Float.NaN);
                    }
                    float wsim = computeWeightedSimilarity(ssim, sims.get("lsim").get(pair), wStruct);
                    sims.get("ssim").put(pair, ssim);
                    sims.get("wsim").put(pair, wsim);
                }
                if (sims.get("wsim").containsKey(pair)) {
                    if (sims.get("wsim").get(pair) > thHigh) {
                        StructuralSimilarity.changeStructuralSimilarity(s.leaves(), t.leaves(), sims, cInc);
                    }
                    if (sims.get("wsim").get(pair) < thLow) {
                        StructuralSimilarity.changeStructuralSimilarity(s.leaves(), t.leaves(), sims, cDec);
                    }
                }
            }
        }
        return sims;
    }

    private Map<String, Map<StringPair, Float>> getSims(
            List<SchemaElementNode> sLeaves,
            List<SchemaElementNode> tLeaves,
            Map<String, Map<String, Double>> compatibilityTable,
            Map<StringPair, Float> lsims,
            float leafWStruct) {
        Map<String, Map<StringPair, Float>> sims = new HashMap<>();
        Map<StringPair, Float> lsim = lsims;
        Map<StringPair, Float> ssim = new HashMap<>();
        Map<StringPair, Float> wsim = new HashMap<>();
        for (SchemaElementNode s : sLeaves) {
            for (SchemaElementNode t : tLeaves) {
                if (compatibilityTable.containsKey(s.current.getDataType()) && compatibilityTable.containsKey(t.current.getDataType())) {
                    StringPair pair = new StringPair(s.name, t.name);
                    float ssimVal = compatibilityTable.get(s.current.getDataType()).get(s.current.getDataType()).floatValue();
                    ssim.put(pair, ssimVal);
                    float lsimValue = 0f;
                    if (lsim.get(pair) != null) lsimValue = lsim.get(pair);
                    float wsimValue = computeWeightedSimilarity(ssimVal, lsimValue, leafWStruct);
                    wsim.put(pair, wsimValue);
                }
            }
        }
        sims.put("ssim", ssim);
        sims.put("lsim", lsim);
        sims.put("wsim", wsim);
        return sims;
    }

    private Map<String, Map<StringPair, Float>> recomputewsim(SchemaTree sourceTree, SchemaTree targetTree, Map<String, Map<StringPair, Float>> sims, float wStruct, float thAccept) {
        List<SchemaElementNode> sPostOrder = sourceTree.postOrder();
        List<SchemaElementNode> tPostOrder = targetTree.postOrder();

        for (SchemaElementNode s: sPostOrder) {
            for (SchemaElementNode t: tPostOrder) {
                if (s.height() == t.height() && (s.height() > 0 && t.height() > 0)) {
                    float ssim = StructuralSimilarity.computeSSim(s,t,sims,thAccept);

                    if (Float.isNaN(ssim)) {
                        continue;
                    }
                    StringPair pair = new StringPair(s.name,t.name);
                    float lsim;
                    lsim = sims.get("lsim").getOrDefault(pair, 0f);
                    float wsim = computeWeightedSimilarity(ssim,lsim,wStruct);
                    sims.get("ssim").put(pair,ssim);
                    sims.get("lsim").put(pair,lsim);
                    sims.get("wsim").put(pair,wsim);
                }
            }
        }
        return sims;
    }

    private float computeWeightedSimilarity(float ssim, float lsim, float wStruct) {
        return ssim * wStruct + (1 - wStruct) * lsim;
    }
}
