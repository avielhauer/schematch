package de.uni_marburg.schematch.evaluation.metric;

import de.uni_marburg.schematch.utils.MetricUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class AUCPR extends AUCMetric {
    @Override
    public float run(int[] groundTruthVector, float[] simVector) {
        List<Integer> sortedSimIndices = MetricUtils.getSortedSimIndices(simVector, groundTruthVector);
        List<Integer> groundTruthIndices = MetricUtils.getGroundTruthIndices(groundTruthVector);

        int n = groundTruthVector.length;
        int numPositives = groundTruthIndices.size();

        int numTP = 0;
        int numFP = 0;
        int numFN = numPositives;

        double[] simVectorDbl = IntStream.range(0, simVector.length).mapToDouble(i -> simVector[i]).toArray(); // TODO: remove after migrated from float to double
        int numThresholds = 1 + (int) Arrays.stream(simVectorDbl).distinct().count(); // first threshold is inf

        double[] precision = new double[numThresholds];
        double[] recall = new double[numThresholds];
        // start ROC at (0,0) for first threshold (inf)
        precision[0] = 1;
        recall[0] = 0;

        float currThreshold = simVector[sortedSimIndices.get(0)];
        int currThresholdIdx = 1;
        // iterate over sim values in descending order and group them by equality (=same threshold)
        for (int i = 0; i < n; i++) {
            float currSimValue = simVector[sortedSimIndices.get(i)];
            if (currSimValue != currThreshold) {
                precision[currThresholdIdx] = numTP > 0 ? (double) numTP / (numTP + numFP) : 0;
                recall[currThresholdIdx] = numTP > 0 ? (double) numTP / (numTP + numFN) : 0;
                currThresholdIdx += 1;
                currThreshold = currSimValue;
            }
            if (groundTruthIndices.contains(sortedSimIndices.get(i))) {
                numTP += 1;
                numFN -= 1;
            } else {
                numFP += 1;
            }
        }
        precision[currThresholdIdx] = numTP > 0 ? (double) numTP / (numTP + numFP) : 0;
        recall[currThresholdIdx] = numTP > 0 ? (double) numTP / (numTP + numFN) : 0;

        return (float) calcAreaUnderCurve(recall, precision);
    }
}
