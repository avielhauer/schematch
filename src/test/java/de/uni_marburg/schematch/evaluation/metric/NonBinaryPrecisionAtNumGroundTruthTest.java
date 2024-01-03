package de.uni_marburg.schematch.evaluation.metric;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NonBinaryPrecisionAtNumGroundTruthTest {

    @Test
    void run() {
        float[] simVector = {0.5f, 0.7f, 0.2f, 0.9f};
        int[] gtVector = {1,0,0,1};

        NonBinaryPrecisionAtNumGroundTruth metric = new NonBinaryPrecisionAtNumGroundTruth();
        float expected = (0.9f)/(0.9f+0.7f);
        float actual = metric.run(gtVector, simVector);

        assertEquals(expected, actual, 0.05f);
    }
}