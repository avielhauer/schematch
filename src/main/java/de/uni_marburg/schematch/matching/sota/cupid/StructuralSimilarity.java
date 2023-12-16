package de.uni_marburg.schematch.matching.sota.cupid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StructuralSimilarity {
    public static double computeSSim(
            SchemaElementNode s,
            SchemaElementNode t,
            HashMap<String,HashMap<StringPair,Double>> sims,
            double th_accept) {
        return Double.NaN;
    }

    public static void changeStructuralSimilarity(
            List<SchemaElementNode> sLeaves,
            List<SchemaElementNode> tLeaves,
            HashMap<String, HashMap<StringPair, Double>> sims,
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
