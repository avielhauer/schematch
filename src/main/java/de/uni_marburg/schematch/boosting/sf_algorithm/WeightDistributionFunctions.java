package de.uni_marburg.schematch.boosting.sf_algorithm;

import de.uni_marburg.schematch.data.Column;

import java.util.HashMap;

public class WeightDistributionFunctions {

    /**
     * Water Weighting refers to the concept of each node spreading 100% of its similarity to all its outgoing edges
     * equally.
     * @param edge Edge of pGraph
     * @param pGraph Propagation Graph
     * @return weight of edge
     */
    public static float waterWeighting(WeightedEdge edge, PropagationGraph pGraph){
        return cpWaterWeighting(edge, pGraph, 1);
    }

    /**
     * Inversed Water Weighting refers to the concept of each node getting 100% of its added similarity from all its
     * incoming edges equally.
     * @param edge Edge of pGraph
     * @param pGraph Propagation Graph
     * @return weight of edge
     */
    public static float inversedWaterWeighting(WeightedEdge edge, PropagationGraph pGraph){
        return cpInversedWaterWeighting(edge, pGraph, 1);
    }

    /**
     * Column Preferred Water Weighting refers to the concept of each node getting 100% of its added similarity from all
     * its incoming edges equally, but the weight of edges targeting a column is multiplied by columnFactor
     * @param edge Edge of pGraph
     * @param pGraph Propagation Graph
     * @return weight of edge
     */
    public static float cpWaterWeighting(WeightedEdge edge, PropagationGraph pGraph, float columnFactor){
        ObjectPair target = pGraph.getEdgeTarget(edge);
        if (target.objectA().getClass() == Column.class){
            return (float) (1.0/pGraph.outgoingEdgesOf(pGraph.getEdgeSource(edge)).size()) * columnFactor;
        }
        return (float) (1.0/pGraph.outgoingEdgesOf(pGraph.getEdgeSource(edge)).size());
    }

    /**
     * Inversed Water Weighting refers to the concept of each node getting 100% of its added similarity from all its
     * incoming edges equally , but the weight of edges targeting a column ObjectPair is multiplied by columnFactor
     * @param edge Edge of pGraph
     * @param pGraph Propagation Graph
     * @return weight of edge
     */
    public static float cpInversedWaterWeighting(WeightedEdge edge, PropagationGraph pGraph, float columnFactor){
        ObjectPair target = pGraph.getEdgeTarget(edge);
        if (target.objectA().getClass() == Column.class){
            return (float) (1.0/ pGraph.incomingEdgesOf(target).size()) * columnFactor;
        }
        return (float) (1.0/ pGraph.incomingEdgesOf(pGraph.getEdgeTarget(edge)).size());
    }


}