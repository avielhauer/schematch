package de.uni_marburg.schematch.boosting.sf_algorithm;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import java.util.HashMap;
import java.util.Map;

public class PropagationGraph extends SimpleDirectedGraph<ObjectPair, WeightedEdge> {

    private Map<ObjectPair, Float> defaultSimilarityMap = new HashMap();
    private Map<ObjectPair, Float> similarityMap = new HashMap();

    public PropagationGraph() {
        super(WeightedEdge.class);
    }

    public void floodingStepA(){
        Map<ObjectPair, Float> newSimilarityMap = new HashMap<>(similarityMap);
        float maximum = Float.MIN_VALUE;
        for(WeightedEdge edge : this.edgeSet()){
            ObjectPair source = getEdgeSource(edge);
            ObjectPair target = getEdgeTarget(edge);
            float oldValue =  newSimilarityMap.get(target);
            float bonus = edge.getWeight() * similarityMap.get(source);
            if(maximum < oldValue + bonus) maximum = oldValue + bonus;
            newSimilarityMap.put(target, oldValue + bonus);
        }
        for (ObjectPair key : newSimilarityMap.keySet()){
            newSimilarityMap.put(key, newSimilarityMap.get(key)/maximum);
        }
        this.similarityMap = newSimilarityMap;
    }


}
