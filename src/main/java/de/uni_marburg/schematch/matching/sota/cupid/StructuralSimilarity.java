package de.uni_marburg.schematch.matching.sota.cupid;

import java.util.*;

public class StructuralSimilarity {
    public static float computeSSim(
            SchemaElementNode s,
            SchemaElementNode t,
            Map<String, Map<StringPair, Float>> sims,
            float th_accept
    ) {
        List<SchemaElementNode> sLeaves = s.leaves();
        List<SchemaElementNode> tLeaves = t.leaves();

        if (s.isLeave() && t.isLeave() ) {
            return sims.get("ssim").get(new StringPair(s.name,t.name));
        }

        if (sLeaves.size() > tLeaves.size()*2 || sLeaves.size()*2 < tLeaves.size()) return Float.NaN;

        HashSet<SchemaElementNode> sStrongLink = new HashSet<SchemaElementNode>();
        HashSet<SchemaElementNode> tStrongLink = new HashSet<SchemaElementNode>();

        for (SchemaElementNode s1: sLeaves) {
            for (SchemaElementNode t1: tLeaves) {
                StringPair pair = new StringPair(s1.name,t1.name);
                if (sims.get("wsim").get(pair) != null && sims.get("wsim").get(pair) > th_accept) {
                    sStrongLink.add(s1);
                    tStrongLink.add(t1);
                }
            }
        }
        return  (float) (sStrongLink.size() + tStrongLink.size()) / (sLeaves.size() + tLeaves.size());
    }

    public static void changeStructuralSimilarity(
            List<SchemaElementNode> sLeaves,
            List<SchemaElementNode> tLeaves,
            Map<String, Map<StringPair, Float>> sims,
            float cInc
    ) {
        List<StringPair> allLeaves = product(sLeaves, tLeaves);

        for (StringPair pair: allLeaves) {
            if (sims.get("ssim").containsKey(pair)) {
                float partial = sims.get("ssim").get(pair) * cInc;
                if (partial > 1) partial = 1.0f;
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
