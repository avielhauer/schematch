package de.uni_marburg.schematch.evaluation.metric;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NonBinaryPrecisionAtGroundTruthTest {

    @Test
    void run() {
        float[] simVector = {0.5f, 0.7f, 0.2f, 0.9f};
        int[] gtVector = {1,0,0,1};

        NonBinaryPrecisionAtGroundTruth metric = new NonBinaryPrecisionAtGroundTruth();
        float expected = (0.9f+0.5f)/(0.9f+0.7f+0.5f);
        float actual = metric.run(gtVector, simVector);

        assertEquals(expected, actual, 0.05f);
    }
}