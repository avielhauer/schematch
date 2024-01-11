package de.uni_marburg.schematch.boosting;

import de.uni_marburg.schematch.matchtask.MatchTask;
import de.uni_marburg.schematch.matchtask.matchstep.SimMatrixBoostingStep;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Dummy similarity matrix boosting which simply returns the input similarity matrix as boosted similarity matrix
 */
public class IdentitySimMatrixBoosting implements SimMatrixBoosting {
    private final static Logger log = LogManager.getLogger(IdentitySimMatrixBoosting.class);

    @Override
    public float[][] run(MatchTask matchTask, SimMatrixBoostingStep matchStep, float[][] simMatrix) {
        log.trace("Matrix: " + simMatrix.length + " x " + simMatrix[0].length);
        return simMatrix;
    }
}
