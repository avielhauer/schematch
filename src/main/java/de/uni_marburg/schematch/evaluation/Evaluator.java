package de.uni_marburg.schematch.evaluation;

import de.uni_marburg.schematch.data.Scenario;
import de.uni_marburg.schematch.evaluation.metric.Metric;
import de.uni_marburg.schematch.evaluation.performance.Performance;
import de.uni_marburg.schematch.utils.ArrayUtils;
import de.uni_marburg.schematch.utils.Configuration;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class Evaluator {
    final static Logger log = LogManager.getLogger(Evaluator.class);

    private final List<Metric> metrics;
    private final Scenario scenario;
    private final int[][] groundTruthMatrix;
    private final int[] groundTruthVector;
    private final int numGroundTruth;
    private List<Integer> sourceGroundTruthIndices;
    private List<Integer> targetGroundTruthIndices;
    private int[][] transposedGroundTruthMatrix;

    public Evaluator(List<Metric> metrics, Scenario scenario, int[][] groundTruthMatrix) {
        this.metrics = metrics;
        this.scenario = scenario;
        this.groundTruthMatrix = groundTruthMatrix;
        this.groundTruthVector = ArrayUtils.flattenMatrix(groundTruthMatrix);
        this.numGroundTruth = ArrayUtils.sumOfMatrix(groundTruthMatrix);

        if (Configuration.getInstance().isEvaluateAttributes()) {
            this.sourceGroundTruthIndices = new ArrayList<>();
            this.targetGroundTruthIndices = new ArrayList<>();
            this.transposedGroundTruthMatrix = ArrayUtils.transposeMatrix(groundTruthMatrix);
            for (int i = 0; i < groundTruthMatrix.length; i++) {
                for (int j = 0; j < groundTruthMatrix[0].length; j++) {
                    if (groundTruthMatrix[i][j] == 1) {
                        sourceGroundTruthIndices.add(i);
                        targetGroundTruthIndices.add(j);
                    }
                }
            }
        }
    }

    public Map<Metric, Performance> evaluate(float[][] simMatrix) {
        float[] simVector = ArrayUtils.flattenMatrix(simMatrix);
        Map<Metric, Performance> performances = new HashMap<>();

        for (Metric metric : this.metrics) {
            Performance performance = new Performance(metric.run(this.groundTruthVector, simVector));
            if (Configuration.getInstance().isEvaluateAttributes()) {
                assert this.sourceGroundTruthIndices != null;
                assert this.targetGroundTruthIndices != null;
                assert this.transposedGroundTruthMatrix != null;
                for (Integer i : this.sourceGroundTruthIndices) {
                    performance.addSourceAttributeScore(i, metric.run(this.groundTruthMatrix[i], simMatrix[i]));
                }
                float[][] transposedSimMatrix = ArrayUtils.transposeMatrix(simMatrix);
                for (Integer j : this.targetGroundTruthIndices) {
                    performance.addTargetAttributeScore(j, metric.run(this.transposedGroundTruthMatrix[j], transposedSimMatrix[j]));
                }
            }
            performances.put(metric, performance);
        }

        return performances;
    }
}