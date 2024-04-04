package de.uni_marburg.schematch.evaluation.metric;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AUCPRTest {

    @Test
    // expected values taken from Python's sklearn.metrics.roc_auc_score
    void run() {
        AUCPR metric = new AUCPR();

        float[] simVector = {0.1f, 0.4f, 0.4f, 0.8f};
        int[] gtVector = {1,1,0,1};
        float expected = 0.847f;
        float actual = metric.run(gtVector, simVector);
        assertEquals(expected, actual, 0.001f);

        simVector = new float[]{0.2f, 0.2f, 0.2f, 0.8f};
        gtVector = new int[]{1,1,1,0};
        expected = 0.375f;
        actual = metric.run(gtVector, simVector);
        assertEquals(expected, actual, 0.001f);

        simVector = new float[]{0.5f,0.4f,0.4f,0.3f,0.9f,0.35f,0.7f};
        gtVector = new int[]{1,0,0,0,0,1,0};
        expected = 0.216f;
        actual = metric.run(gtVector, simVector);
        assertEquals(expected, actual, 0.001f);
    }
}