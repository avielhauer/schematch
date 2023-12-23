package de.uni_marburg.schematch.boosting.sf_algorithm.propagation_graph;

import de.uni_marburg.schematch.boosting.sf_algorithm.db_2_graph.DBGraph;
import de.uni_marburg.schematch.boosting.sf_algorithm.similarity_calculator.SimilarityCalculator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class InversedWaterWeightingGraph extends PropagationGraph<PropagationNode> {
    private final static Logger log = LogManager.getLogger(InversedWaterWeightingGraph.class);

    public InversedWaterWeightingGraph(DBGraph dbGraphA, DBGraph dbGraphB, SimilarityCalculator similarityCalculator) {
        super(dbGraphA, dbGraphB, similarityCalculator);
    }

    @Override
    protected PropagationNode createNode(Object objectA, Object objectB) {
        return new PropagationNode(objectA, objectB);
    }

    @Override
    protected void generateEdgeWeight(WeightedEdge edge) {
        edge.setWeight(1F/this.incomingEdgesOf(this.getEdgeTarget(edge)).size());
    }
}
