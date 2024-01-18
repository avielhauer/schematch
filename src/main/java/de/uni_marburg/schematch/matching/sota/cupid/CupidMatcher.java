package de.uni_marburg.schematch.matching.sota.cupid;

import de.uni_marburg.schematch.data.Table;
import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;

import java.util.*;

/**
 * TODO: Implement Cupid Matcher
 */
public class CupidMatcher extends Matcher {
    @Override
    public float[][] match(TablePair tablePair) {
        return new float[0][];
    }

    // Gibt eine Map zurück mit 3 Schlüsseln "ssim","lsim" und "wsim"
    // mit denen auf 3 weitere Maps zugegriffen werden kann, welche
    // die Namenspaare der beiden bäume als schlüssel haben.
    private Map<String,Map<StringPair,Float>> treeMatch(
            SchemaTree sourceTree,
            SchemaTree targetTree,
            List<String> categories,
            float leafWStruct,
            float wStruct,
            float thAccept,
            float thHigh,
            float thLow,
            float cInc,
            float cDec,
            float thNs,
            int parallelism
    ) {
        Map<String, Map<String, Double>> compatibilityTable = new LinguisticMatching().computeCompatibility(categories);
        Map<StringPair,Double> lSims = new LinguisticMatching().comparison(sourceTree,targetTree,compatibilityTable,thNs,parallelism);
        List<SchemaElementNode> sLeaves = sourceTree.getLeaves();
        List<SchemaElementNode> tLeaves = targetTree.getLeaves();
        Map<String,Map<StringPair,Float>> sims = getSims(sLeaves, tLeaves, compatibilityTable, lSims, leafWStruct);
        List<SchemaElementNode> sPostOrder = sourceTree.postOrder();
        List<SchemaElementNode> tPostOrder = targetTree.postOrder();

        for (SchemaElementNode s : sPostOrder) {
            for (SchemaElementNode t : tPostOrder) {
                StringPair pair = new StringPair(s.name,t.name);
                if (s.height() == t.height()) {
                    float ssim = StructuralSimilarity.computeSSim(s, t, sims, thAccept);
                    if (Double.isNaN(ssim)) {
                        continue;
                    }
                    if(!lSims.containsKey(pair)) {
                        sims.get("lsim").put(pair,Float.NaN);
                    }
                    float wsim = computeWeightedSimilarity(ssim,lSims.get(pair).floatValue(), wStruct);
                    sims.get("ssim").put(pair,ssim);
                    sims.get("wsim").put(pair,wsim);
                }
                if(sims.get("wsim").containsKey(pair)) {
                    if (sims.get("wsim").get(pair) > thHigh) {
                        StructuralSimilarity.changeStructuralSimilarity(s.leaves(),t.leaves(),sims,cInc);
                    }
                    if (sims.get("wsim").get(pair) < thLow) {
                        StructuralSimilarity.changeStructuralSimilarity(s.leaves(),t.leaves(),sims,cDec);
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
            Map<StringPair, Double> lsimsDouble,
            float leafWStruct) {
        Map<String, Map<StringPair, Float>> sims = new HashMap<String, Map<StringPair, Float>>();
        Map<StringPair, Float> lsim = doubleDictToFloat(lsimsDouble);
        Map<StringPair, Float> ssim = new HashMap<StringPair, Float>();
        Map<StringPair, Float> wsim = new HashMap<StringPair, Float>();
        for (SchemaElementNode s: sLeaves) {
            for (SchemaElementNode t: tLeaves) {
                if (compatibilityTable.containsKey(s.current.getDataType()) && compatibilityTable.containsKey(t.current.getDataType())) {
                    StringPair pair = new StringPair(s.name,t.name);
                    float ssimVal = compatibilityTable.get(s.name).get(t.name).floatValue();
                    ssim.put(pair,ssimVal);
                    wsim.put(pair, computeWeightedSimilarity(ssimVal,lsim.get(pair),leafWStruct));
                }
            }
        }
        sims.put("ssim",ssim);
        sims.put("lsim",lsim);
        sims.put("wsim",wsim);
        return sims;
    }

    private Map<StringPair, Float> doubleDictToFloat(Map<StringPair, Double> dict) {
        Map<StringPair, Float> newDict = new HashMap<>();
        for (StringPair pair: dict.keySet()) {
            newDict.put(pair, dict.get(pair).floatValue());
        }
        return newDict;
    }

    private float computeWeightedSimilarity(float ssim, float lsim, float wStruct) {
        return ssim * wStruct + (1-wStruct) * lsim;
    }
    public static float[][] mappingGenerationLeaves(
            TablePair tablePair,
            Map<String, Map<StringPair, Float>> sims,
            float thAccept
    ) {
        Table sTable = tablePair.getSourceTable();
        Table tTable = tablePair.getTargetTable();

        float[][] simMatrix = tablePair.getEmptySimMatrix();
        for (int s = 0; s < simMatrix.length; s++) {
            for (int t = 0; t < simMatrix[0].length; t++) {
                StringPair pair = new StringPair(sTable.getLabels().get(s),tTable.getLabels().get(t));
                float val = sims.get("wsim").get(pair);
                if (val > thAccept) {
                    simMatrix[s][t] = val;
                }
            }
        }
        return simMatrix;
    }
}








