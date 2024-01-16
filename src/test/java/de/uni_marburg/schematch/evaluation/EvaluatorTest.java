package de.uni_marburg.schematch.evaluation;

import de.uni_marburg.schematch.TestUtils;
import de.uni_marburg.schematch.data.Scenario;
import de.uni_marburg.schematch.evaluation.metric.Metric;
import de.uni_marburg.schematch.evaluation.metric.NonBinaryPrecisionAtGroundTruth;
import de.uni_marburg.schematch.evaluation.performance.Performance;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EvaluatorTest {

    @Test
    void evaluate() {
        TestUtils.TestData testData = TestUtils.getTestData();

        List<Metric> metrics = new ArrayList<>();
        metrics.add(new NonBinaryPrecisionAtGroundTruth());
        Scenario scenario = new Scenario(testData.getScenarios().get("test3").getPath());
        // FIXME: refactor reading of ground truth to be more accessible
        int[][] groundTruthMatrix = {
                {1,0,0,0},
                {0,0,0,0},
                {0,0,1,0},
                {0,0,0,0}
        };

        float[][] simMatrix = {
                {0.5f,0.6f,0.4f,0.3f},
                {0.5f,0.3f,0.2f,0.0f},
                {0.9f,0.2f,0.6f,0.8f},
                {0.3f,0.2f,0.0f,0.3f}
        };

        Evaluator evaluator = new Evaluator(metrics, scenario, groundTruthMatrix);
        Performance performance = evaluator.evaluate(simMatrix).get(metrics.get(0));

        float expectedGlobalScore = (0.5f+0.6f)/(0.5f+0.6f+0.5f+0.9f+0.6f+0.8f);
        float expectedSourceAttributeScore0 = (0.5f)/(0.5f+0.6f);
        float expectedSourceAttributeScore2 = (0.6f)/(0.9f+0.6f+0.8f);
        float expectedTargetAttributeScore0 = (0.5f)/(0.5f+0.5f+0.9f);
        float expectedTargetAttributeScore2 = 1.0f;

        assertEquals(expectedGlobalScore, performance.getGlobalScore(), 0.05);
        assertEquals(expectedSourceAttributeScore0, performance.getSourceAttributeScores().get(0), 0.05);
        assertEquals(expectedSourceAttributeScore2, performance.getSourceAttributeScores().get(2), 0.05);
        assertEquals(expectedTargetAttributeScore0, performance.getTargetAttributeScores().get(0), 0.05);
        assertEquals(expectedTargetAttributeScore2, performance.getTargetAttributeScores().get(2), 0.05);
    }
}