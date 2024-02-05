package de.uni_marburg.schematch.boosting.sf_algorithm.propagation_graph;

import de.uni_marburg.schematch.boosting.sf_algorithm.db_2_graph.DBGraph;
import de.uni_marburg.schematch.boosting.sf_algorithm.similarity_calculator.SimilarityCalculator;

public class ConstantWeightingGraph extends PropagationGraph{
    final float weight = 1f;
    public ConstantWeightingGraph(DBGraph dbGraphA, DBGraph dbGraphB, SimilarityCalculator similarityCalculator) {
        super(dbGraphA, dbGraphB, similarityCalculator);
    }

    @Override
    protected PropagationNode createNode(Object objectA, Object objectB) {
        return new PropagationNode(objectA, objectB);
    }

    @Override
    protected void generateEdgeWeight(WeightedEdge edge) {
        edge.setWeight(this.weight);
    }
}
