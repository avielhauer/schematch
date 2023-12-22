package de.uni_marburg.schematch.evaluation;

import de.uni_marburg.schematch.data.Scenario;
import de.uni_marburg.schematch.evaluation.metric.Metric;
import de.uni_marburg.schematch.evaluation.performance.Performance;
import de.uni_marburg.schematch.utils.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Evaluator {
    final static Logger log = LogManager.getLogger(Evaluator.class);

    private final Scenario scenario;
    private final int[] groundTruthVector;
    private final int numGroundTruth;
    private final List<Metric> metrics;

    public Evaluator(List<Metric> metrics, Scenario scenario, int[][] groundTruthMatrix) {
        this.metrics = metrics;
        this.scenario = scenario;
        this.numGroundTruth = ArrayUtils.sumOfMatrix(groundTruthMatrix);
        this.groundTruthVector = ArrayUtils.flattenMatrix(groundTruthMatrix);
    }

    public Map<Metric, Performance> evaluate(float[][] simMatrix) {
        float[] simVector = ArrayUtils.flattenMatrix(simMatrix);
        Map<Metric, Performance> performances = new HashMap<>();

        for (Metric metric : this.metrics) {
            performances.put(metric, metric.run(this.groundTruthVector, simVector));
        }

        return performances;
    }
}
