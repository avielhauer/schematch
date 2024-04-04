package de.uni_marburg.schematch.evaluation.metric;

import de.uni_marburg.schematch.utils.MetricUtils;

import java.util.*;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

public class AUROC extends AUCMetric {
    @Override
    public float run(int[] groundTruthVector, float[] simVector) {
        List<Integer> sortedSimIndices = MetricUtils.getSortedSimIndices(simVector, groundTruthVector);
        List<Integer> groundTruthIndices = MetricUtils.getGroundTruthIndices(groundTruthVector);

        int n = groundTruthVector.length;
        int numPositives = groundTruthIndices.size();
        int numNegatives = n - numPositives;

        int numTP = 0;
        int numFP = 0;

        double[] simVectorDbl = IntStream.range(0, simVector.length).mapToDouble(i -> simVector[i]).toArray(); // TODO: remove after migrated from float to double
        int numThresholds = 1 + (int) Arrays.stream(simVectorDbl).distinct().count(); // first threshold is inf

        double[] tpr = new double[numThresholds];
        double[] fpr = new double[numThresholds];
        // start ROC at (0,0) for first threshold (inf)
        tpr[0] = 0;
        fpr[0] = 0;

        float currThreshold = simVector[sortedSimIndices.get(0)];
        int currThresholdIdx = 1;
        // iterate over sim values in descending order and group them by equality (=same threshold)
        for (int i = 0; i < n; i++) {
            float currSimValue = simVector[sortedSimIndices.get(i)];
            if (currSimValue != currThreshold) {
                tpr[currThresholdIdx] = (double) numTP / numPositives;
                fpr[currThresholdIdx] = (double) numFP / numNegatives;
                currThresholdIdx += 1;
                currThreshold = currSimValue;
            }
            if (groundTruthIndices.contains(sortedSimIndices.get(i))) {
                numTP += 1;
            } else {
                numFP += 1;
            }
        }
        tpr[currThresholdIdx] = (double) numTP / numPositives;
        fpr[currThresholdIdx] = (double) numFP / numNegatives;

        return (float) calcAreaUnderCurve(fpr, tpr);
    }
}
