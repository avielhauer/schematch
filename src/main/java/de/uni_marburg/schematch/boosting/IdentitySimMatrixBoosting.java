package de.uni_marburg.schematch.boosting;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Dummy similarity matrix boosting which simply returns the input similarity matrix as boosted similarity matrix
 */
public class IdentitySimMatrixBoosting implements SimMatrixBoosting {
    private final static Logger log = LogManager.getLogger(IdentitySimMatrixBoosting.class);

    @Override
    public float[][] run(float[][] simMatrix) {
        return simMatrix;
    }
}
