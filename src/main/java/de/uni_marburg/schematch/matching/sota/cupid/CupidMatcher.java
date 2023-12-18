package de.uni_marburg.schematch.matching.sota.cupid;

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
    private Map<String,Map<StringPair,Double>> treeMatch(
            SchemaTree sourceTree,
            SchemaTree targetTree,
            List<String> categories,
            double leafWStruct,
            double wStruct,
            double thAccept,
            double thHigh,
            double thLow,
            double cInc,
            double cDec,
            double thNs,
            int parallelism
    ) {
        Map<String, Map<String, Double>> compatibilityTable = new LinguisticMatching().computeCompatibility(categories);
        Map<StringPair,Double> lSims = new LinguisticMatching().comparison(sourceTree,targetTree,compatibilityTable,thNs,parallelism);
        List<SchemaElementNode> sLeaves = sourceTree.getLeaves();
        List<SchemaElementNode> tLeaves = targetTree.getLeaves();
        Map<String,Map<StringPair,Double>> sims = getSims(sLeaves, tLeaves, compatibilityTable, lSims, leafWStruct);
        List<SchemaElementNode> sPostOrder = sourceTree.postOrder();
        List<SchemaElementNode> tPostOrder = targetTree.postOrder();

        for (SchemaElementNode s : sPostOrder) {
            for (SchemaElementNode t : tPostOrder) {
                StringPair pair = new StringPair(s.name,t.name);
                if (s.height() == t.height()) {
                    double ssim = StructuralSimilarity.computeSSim(s, t, sims, thAccept);
                    if (Double.isNaN(ssim)) {
                        continue;
                    }
                    if(!lSims.containsKey(pair)) {
                        lSims.put(pair,Double.NaN);
                    }
                    double wsim = computeWeightedSimilarity(ssim,lSims.get(pair), wStruct);
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

    private Map<String, Map<StringPair, Double>> getSims(
            List<SchemaElementNode> sLeaves,
            List<SchemaElementNode> tLeaves,
            Map<String, Map<String, Double>> compatibilityTable,
            Map<StringPair, Double> lsims,
            double leafWStruct) {
        Map<String, Map<StringPair, Double>> sims = new HashMap<String, Map<StringPair, Double>>();
        Map<StringPair, Double> ssim = new HashMap<StringPair, Double>();
        Map<StringPair, Double> wsim = new HashMap<StringPair, Double>();
        for (SchemaElementNode s: sLeaves) {
            for (SchemaElementNode t: tLeaves) {
                if (compatibilityTable.containsKey(s.current.getDataType()) && compatibilityTable.containsKey(t.current.getDataType())) {
                    StringPair pair = new StringPair(s.name,t.name);
                    double ssimVal = compatibilityTable.get(s.name).get(t.name);
                    ssim.put(pair,ssimVal);
                    wsim.put(pair, computeWeightedSimilarity(ssimVal,lsims.get(pair),leafWStruct));
                }
            }
        }
        sims.put("ssim",ssim);
        sims.put("lsim",lsims);
        sims.put("wsim",wsim);
        return sims;
    }

    private double computeWeightedSimilarity(double ssim, double lsim, double wStruct) {
        return ssim * wStruct + (1-wStruct) * lsim;
    }
}








