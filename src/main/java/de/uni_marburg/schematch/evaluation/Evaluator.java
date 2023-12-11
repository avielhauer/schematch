package de.uni_marburg.schematch.evaluation;

public interface Evaluator<T> {
    public float evaluateMatrix(T simMatrix, int[][] groundTruthMatrix);
}
