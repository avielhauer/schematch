package de.uni_marburg.schematch.boosting;

/**
 * Dummy similarity matrix boosting which simply returns the input similarity matrix as boosted similarity matrix
 */
public class IdentitySimMatrixBoosting implements SimMatrixBoosting {
    @Override
    public float[][] run(float[][] simMatrix) {
        return simMatrix;
    }
}
