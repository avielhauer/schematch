package de.uni_marburg.schematch.boosting;

public interface SimMatrixBoosting {
    /**
     * @param simMatrix similarity matrix to improve
     * @return An updated (hopefully better) similarity matrix
     */
    public float[][] run(float[][] simMatrix);
}
