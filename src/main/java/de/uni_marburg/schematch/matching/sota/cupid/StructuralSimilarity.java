package de.uni_marburg.schematch.matching.sota.cupid;

import java.util.*;

public class StructuralSimilarity {
    public static double computeSSim(
            SchemaElementNode s,
            SchemaElementNode t,
            Map<String,Map<StringPair,Double>> sims,
            double th_accept) {
        List<SchemaElementNode> sLeaves = s.leaves();
        List<SchemaElementNode> tLeaves = t.leaves();

        if (sLeaves.size() > tLeaves.size()*2 || sLeaves.size()*2 < tLeaves.size()) return Double.NaN;

        List<SchemaElementNode> sStrongLink = new ArrayList<SchemaElementNode>();
        List<SchemaElementNode> tStrongLink = new ArrayList<SchemaElementNode>();

        for (SchemaElementNode s1: sLeaves) {
            for (SchemaElementNode t1: sLeaves) {
                StringPair pair = new StringPair(s1.name,t1.name);
                if (sims.get("wsim").get(pair) > th_accept) {
                    sStrongLink.add(s1);
                    tStrongLink.add(t1);
                }
            }
        }
        return (double) (sStrongLink.size() + tStrongLink.size()) / (sLeaves.size() + tLeaves.size());
    }

    public static void changeStructuralSimilarity(
            List<SchemaElementNode> sLeaves,
            List<SchemaElementNode> tLeaves,
            Map<String, Map<StringPair, Double>> sims,
            double cInc) {
        List<StringPair> allLeaves = product(sLeaves, tLeaves);

        for (StringPair pair: allLeaves) {
            if (sims.get("ssim").containsKey(pair)) {
                double partial = sims.get("ssim").get(pair) * cInc;
                if (partial > 1) partial = 1.0;
                sims.get("ssim").put(pair,partial);
            }
        }
    }

    public static List<StringPair> product(List<SchemaElementNode> sLeaves, List<SchemaElementNode> tLeaves) {
        ArrayList<StringPair> productList = new ArrayList<StringPair>();
        for (SchemaElementNode s: sLeaves) {
            for (SchemaElementNode t: tLeaves) {
                productList.add(new StringPair(s.name,t.name));
            }
        }
        return productList;
    }
}
