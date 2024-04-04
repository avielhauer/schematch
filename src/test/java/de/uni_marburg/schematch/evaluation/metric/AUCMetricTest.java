package de.uni_marburg.schematch.evaluation.metric;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AUCMetricTest {

    static class AUMock extends AUCMetric {
        @Override
        public float run(int[] groundTruthVector, float[] simVector) {
            return 0;
        }
    }

    @Test
    void calcAreaUnderCurve() {
        double[] x = {0,1,1};
        double[] y = {0,0,1};

        double expected = 0;
        double actual = new AUMock().calcAreaUnderCurve(x, y);

        assertEquals(expected, actual, 0.001);
    }
}