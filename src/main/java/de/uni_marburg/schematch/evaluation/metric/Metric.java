package de.uni_marburg.schematch.evaluation.metric;

public abstract class Metric {
    public abstract float run(int[] groundTruthVector, float[] simVector);

    /**
     * Whether the metric should be run automatically on the similarity matrix output of a matcher.
     * If false, this metric will be created but must be run manually (as with the MatcherRuntime metric, for example)
     */
    public boolean runsOnSimilarityMatrices() {
        return true;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

    public Float aggregatePerformance(float sumScores, int n){
        return sumScores / n;
    }
}
