package de.uni_marburg.schematch.utils;

import java.util.LinkedList;
import java.util.List;

public class MetricUtils {


    /**
     * @param groundTruthVector
     * @param simVector
     * @return List of indices sorted in descending order of similarity values in {@code simVector}.
     *      When two values are equal, ground truth works as tie breaker: negatives are listed before positives.
     */
    public static List<Integer> getSortedSimIndices(int[] groundTruthVector, float[] simVector) {
        List<Integer> res = new LinkedList<>();
        List<Integer> gtIndices = getGroundTruthIndices(groundTruthVector);

        // insert all sim indices not in ground truth
        for (int i = 0; i < simVector.length; i++) {
            if (!gtIndices.contains(i)) {
                res.add(i);
            }
        }

        res.sort((a,b) -> Float.compare(simVector[b], simVector[a]));

        // add all sim indices which are in ground truth
        for (int id : gtIndices) {
            float currSimScore = simVector[id];
            int currResSize = res.size();
            boolean inserted = false;
            for (int i = 0; i < currResSize; i++) {
                if (simVector[res.get(i)] < currSimScore) {
                    res.add(i, id);
                    inserted = true;
                    break;
                }
            }
            if (!inserted) { // add to end
                res.add(id);
            }
        }

        return res;
    }

    public static List<Integer> getGroundTruthIndices(int[] groundTruthVector) {
        List<Integer> res = new LinkedList<>();

        for (int i = 0; i < groundTruthVector.length; i++) {
            if (groundTruthVector[i] == 1) {
                res.add(i);
            }
        }

        return res;
    }
}
