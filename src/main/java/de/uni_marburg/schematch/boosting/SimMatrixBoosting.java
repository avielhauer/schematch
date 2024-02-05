package de.uni_marburg.schematch.boosting;


import de.uni_marburg.schematch.matching.Matcher;
import de.uni_marburg.schematch.matchtask.MatchTask;
import de.uni_marburg.schematch.matchtask.matchstep.SimMatrixBoostingStep;

public interface SimMatrixBoosting {
    /**
     * @param matchTask MatchTask to boost sim matrix for
     * @param matchStep Current MatchStep (SimMatrixBoostingStep)
     * @param simMatrix similarity matrix to improve
     * @param matcher
     * @return An updated (hopefully better) similarity matrix
     */
    public default float[][] run(MatchTask matchTask, SimMatrixBoostingStep matchStep, float[][] simMatrix, Matcher matcher){
        return run(matchTask, matchStep, simMatrix);
    }

    public float[][] run(MatchTask matchTask, SimMatrixBoostingStep matchStep, float[][] simMatrix);
}
