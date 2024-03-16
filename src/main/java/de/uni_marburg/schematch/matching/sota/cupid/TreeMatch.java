package de.uni_marburg.schematch.matching.sota.cupid;

import de.uni_marburg.schematch.matchtask.tablepair.TablePair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TreeMatch {
    /**
     * Runs the Cupid TreeMatch algorithm
     * @param sourceTree source SchemaTree
     * @param targetTree target SchemaTree
     * @param categories List of data types in the schema tree´s
     * @param leafWStruct Weight of the structural similarity, to calculate the weighted similarity between structural
     *                    similarity and linguistic similarity of the leaf node pairs
     * @param wStruct Weight of the structural similarity, to calculate the weighted similarity between structural
     *                similarity and linguistic similarity of the non-leaf node pairs
     * @param thAccept Threshold for mapping and strong links in the structural matching
     * @param thHigh Threshold for the weighted similarity between non-leaf nodes to decide if children nodes ssim
     *               should be increase
     * @param thLow  Threshold for the weighted similarity between non-leaf nodes to decide if children nodes ssim
     *               should be decreased
     * @param cInc Factor by which the ssim values should be increased by
     * @param cDec Factor by which the ssim values should be decreased by
     * @param thNs Threshold to filter out nodepairs, which have a data compatabilty less than thNs
     * @param parallelism Threads to run the linguistic matching on.
     * @param linguisticMatching LinguisticMatching instance
     * @return weighted, structural and linguistic similarity hashmaps with the string pair´s of the names of the node
     *         pair´s as keys, in a hashmap with keys={"wsim","lsim","ssim"}
     */
    public Map<String, Map<StringPair, Float>> treeMatch(
            SchemaTree sourceTree,
            SchemaTree targetTree,
            Set<String> categories,
            float leafWStruct,
            float wStruct,
            float thAccept,
            float thHigh,
            float thLow,
            float cInc,
            float cDec,
            float thNs,
            int parallelism,
            LinguisticMatching linguisticMatching
    ) {
        Map<String, Map<String, Double>> compatibilityTable = linguisticMatching.computeCompatibility(categories);
        Map<StringPair, Float> lSims = linguisticMatching.comparison(sourceTree, targetTree, compatibilityTable, thNs, parallelism);
        List<SchemaElementNode> sLeaves = sourceTree.getLeaves();
        List<SchemaElementNode> tLeaves = targetTree.getLeaves();
        Map<String, Map<StringPair, Float>> sims = getSims(sLeaves, tLeaves, compatibilityTable, lSims, leafWStruct);
        List<SchemaElementNode> sPostOrder = sourceTree.postOrder();
        List<SchemaElementNode> tPostOrder = targetTree.postOrder();

        for (SchemaElementNode s : sPostOrder) {
            if (s.isLeave())
                continue;
            for (SchemaElementNode t : tPostOrder) {
                if (t.isLeave())
                    continue;
                StringPair pair = new StringPair(s.name, t.name);
                if (s.height() == t.height()) {
                    float ssim = StructuralSimilarity.computeSSim(s, t, sims, thAccept);
                    if (Float.isNaN(ssim)) {
                        continue;
                    }
                    if (!lSims.containsKey(pair)) {
                        sims.get("lsim").put(pair, 0f);
                    }
                    float wsim = computeWeightedSimilarity(ssim, sims.get("lsim").get(pair), wStruct);
                    sims.get("ssim").put(pair, ssim);
                    sims.get("wsim").put(pair, wsim);
                }
                if (sims.get("wsim").containsKey(pair) &&  !s.isLeave() && !t.isLeave()) {
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

    /**
     * Takes the data compatability values from the data compatability table and combines these with the leafWStruct
     * to calculate wsim values.
     * @param sLeaves source tree leaves
     * @param tLeaves target tree leaves
     * @param compatibilityTable data compatability table
     * @param lsim linguistic similarity values
     * @param leafWStruct Weight of the structural similarity, to calculate the weighted similarity between structural
     *                    similarity and linguistic similarity of the leaf node pairs
     * @return weighted, structural and linguistic similarity hashmaps with the string pair´s of the names of the node
     * pair´s as keys, in a hashmap with keys={"wsim","lsim","ssim"}
     */
    public Map<String, Map<StringPair, Float>> getSims(
            List<SchemaElementNode> sLeaves,
            List<SchemaElementNode> tLeaves,
            Map<String, Map<String, Double>> compatibilityTable,
            Map<StringPair, Float> lsim,
            float leafWStruct) {
        Map<String, Map<StringPair, Float>> sims = new HashMap<>();
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

    /**
     * Recomputes wsim of all non-leaf nodes
     * @param sourceTree source SchemaTree
     * @param targetTree target SchemaTree
     * @param sims weighted, structural and linguistic similarity hashmaps with the string pair´s of the names of the node
     *             pair´s as keys, in a hashmap with keys={"wsim","lsim","ssim"}
     * @param wStruct Weight of the structural similarity, to calculate the weighted similarity between structural
     *        similarity and linguistic similarity of the non-leaf node pairs
     * @param thAccept Threshold for mapping and strong links in the structural matching
     * @param linguisticMatching LinguisticMatching instance
     * @return weighted, structural and linguistic similarity hashmaps with the string pair´s of the names of the node
     * pair´s as keys, in a hashmap with keys={"wsim","lsim","ssim"}
     */
    public Map<String, Map<StringPair, Float>> recomputewsim(SchemaTree sourceTree, SchemaTree targetTree, Map<String, Map<StringPair, Float>> sims, float wStruct, float thAccept, LinguisticMatching linguisticMatching) {
        List<SchemaElementNode> sPostOrder = sourceTree.postOrder();
        List<SchemaElementNode> tPostOrder = targetTree.postOrder();

        for (SchemaElementNode s: sPostOrder) {
            if (s.isLeave())
                continue;
            for (SchemaElementNode t: tPostOrder) {
                if (t.isLeave())
                    continue;
                if (s.height() == t.height() && (s.height() > 0 && t.height() > 0)) {
                    float ssim = StructuralSimilarity.computeSSim(s,t,sims,thAccept);

                    if (Float.isNaN(ssim)) {
                        continue;
                    }
                    StringPair pair = new StringPair(s.name,t.name);
                    float lsim;
                    if (!sims.get("lsim").containsKey(pair)) {
                        lsim = (float) linguisticMatching.computeLsim(s.getCurrent(),t.getCurrent());
                    } else {
                        lsim = sims.get("lsim").get(pair);
                    }
                    float wsim = TreeMatch.computeWeightedSimilarity(ssim,lsim,wStruct);
                    sims.get("ssim").put(pair,ssim);
                    sims.get("lsim").put(pair,lsim);
                    sims.get("wsim").put(pair,wsim);
                }
            }
        }
        return sims;
    }

    /**
     * Computes the weighted similarity
     * @param ssim ssim value
     * @param lsim lsim values
     * @param wStruct weight of ssim
     * @return ssim * wStruct + (1 - wStruct) *  lsim
     */
    public static float computeWeightedSimilarity(float ssim, float lsim, float wStruct) {
        return wStruct * ssim + ((1 - wStruct) * lsim);
    }

    public static Map<StringPair, Float> convertSimMatrix(TablePair tablePair, float[][] simMatrix) {
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

    /**
     * Runs the Cupid TreeMatch algorithm
     * @param sourceTree source SchemaTree
     * @param targetTree target SchemaTree
     * @param categories List of data types in the schema tree´s
     * @param leafWStruct Weight of the structural similarity, to calculate the weighted similarity between structural
     *                    similarity and linguistic similarity of the leaf node pairs
     * @param wStruct Weight of the structural similarity, to calculate the weighted similarity between structural
     *                similarity and linguistic similarity of the non-leaf node pairs
     * @param thAccept Threshold for mapping and strong links in the structural matching
     * @param thHigh Threshold for the weighted similarity between non-leaf nodes to decide if children nodes ssim
     *               should be increase
     * @param thLow  Threshold for the weighted similarity between non-leaf nodes to decide if children nodes ssim
     *               should be decreased
     * @param cInc Factor by which the ssim values should be increased by
     * @param cDec Factor by which the ssim values should be decreased by
     * @param linguisticMatching LinguisticMatching instance
     * @return weighted, structural and linguistic similarity hashmaps with the string pair´s of the names of the node
     *         pair´s as keys, in a hashmap with keys={"wsim","lsim","ssim"}
     */
    public Map<String, Map<StringPair, Float>> treeMatch(SchemaTree sourceTree, SchemaTree targetTree, Map<StringPair, Float> lSims, Set<String> categories, float leafWStruct, float wStruct, float thAccept, float thHigh, float thLow, float cInc, float cDec, LinguisticMatching linguisticMatching) {
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
                        sims.get("lsim").put(pair, 0f);
                    }
                    float wsim = computeWeightedSimilarity(ssim, sims.get("lsim").get(pair), wStruct);
                    sims.get("ssim").put(pair, ssim);
                    sims.get("wsim").put(pair, wsim);
                }
                if (sims.get("wsim").containsKey(pair) &&  !s.isLeave() && !t.isLeave()) {
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

    /**
     * Recomputes wsim of all non-leaf nodes
     * @param sourceTree source SchemaTree
     * @param targetTree target SchemaTree
     * @param sims weighted, structural and linguistic similarity hashmaps with the string pair´s of the names of the node
     *             pair´s as keys, in a hashmap with keys={"wsim","lsim","ssim"}
     * @param wStruct Weight of the structural similarity, to calculate the weighted similarity between structural
     *        similarity and linguistic similarity of the non-leaf node pairs
     * @param thAccept Threshold for mapping and strong links in the structural matching
     * @return weighted, structural and linguistic similarity hashmaps with the string pair´s of the names of the node
     * pair´s as keys, in a hashmap with keys={"wsim","lsim","ssim"}
     */
    public Map<String, Map<StringPair, Float>> recomputewsim(SchemaTree sourceTree, SchemaTree targetTree, Map<String, Map<StringPair, Float>> sims, float wStruct, float thAccept) {
        List<SchemaElementNode> sPostOrder = sourceTree.postOrder();
        List<SchemaElementNode> tPostOrder = targetTree.postOrder();

        for (SchemaElementNode s: sPostOrder) {
            if (s.isLeave())
                continue;
            for (SchemaElementNode t: tPostOrder) {
                if (t.isLeave())
                    continue;
                if (s.height() == t.height() && (s.height() > 0 && t.height() > 0)) {
                    float ssim = StructuralSimilarity.computeSSim(s,t,sims,thAccept);

                    if (Float.isNaN(ssim)) {
                        continue;
                    }
                    StringPair pair = new StringPair(s.name,t.name);
                    float lsim = sims.get("lsim").get(pair);
                    float wsim = TreeMatch.computeWeightedSimilarity(ssim,lsim,wStruct);
                    sims.get("ssim").put(pair,ssim);
                    sims.get("lsim").put(pair,lsim);
                    sims.get("wsim").put(pair,wsim);
                }
            }
        }
        return sims;
    }
}
