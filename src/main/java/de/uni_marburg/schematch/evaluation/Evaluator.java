package de.uni_marburg.schematch.evaluation;

import de.uni_marburg.schematch.data.Scenario;
import de.uni_marburg.schematch.evaluation.metric.Metric;
import de.uni_marburg.schematch.evaluation.metric.NonBinaryPrecisionAtGroundTruth;
import de.uni_marburg.schematch.evaluation.performance.Performance;
import de.uni_marburg.schematch.utils.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Evaluator {
    final static Logger log = LogManager.getLogger(Evaluator.class);

    private Scenario scenario;
    private int[] groundTruthVector;
    private List<Metric> metrics;

    public Evaluator(Scenario scenario, int[][] groundTruthMatrix) {
        this.scenario = scenario;
        this.groundTruthVector = ArrayUtils.flattenMatrix(groundTruthMatrix);
        metrics = new ArrayList<>();
        metrics.add(new NonBinaryPrecisionAtGroundTruth());
    }

    public Map<Metric, Performance> evaluate(float[][] simMatrix) {
        float[] simVector = ArrayUtils.flattenMatrix(simMatrix);
        Map<Metric, Performance> performances = new HashMap<>();

        for (Metric metric : this.metrics) {
            metric.run(this.groundTruthVector, simVector);
        }

        return performances;
    }
}
