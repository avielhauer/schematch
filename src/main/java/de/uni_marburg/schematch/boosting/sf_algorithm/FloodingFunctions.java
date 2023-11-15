package de.uni_marburg.schematch.boosting.sf_algorithm;

import java.util.HashMap;
import java.util.Map;

public class FloodingFunctions {
    public static HashMap<ObjectPair, Float> floodingFunctionA(Map<ObjectPair, Float> defaultSimilarityMap, Map<ObjectPair, Float> similarityMap, PropagationGraph pGraph){
        HashMap<ObjectPair, Float> newSimilarityMap = new HashMap<>(similarityMap);
        float maximum = Float.MIN_VALUE;
        for(WeightedEdge edge : pGraph.edgeSet()){
            ObjectPair source = pGraph.getEdgeSource(edge);
            ObjectPair target = pGraph.getEdgeTarget(edge);
            float oldValue =  newSimilarityMap.get(target);
            float bonus = edge.getWeight() * similarityMap.get(source);
            if(maximum < oldValue + bonus) maximum = oldValue + bonus;
            newSimilarityMap.put(target, oldValue + bonus);
        }

        for (ObjectPair key : newSimilarityMap.keySet()){
            newSimilarityMap.put(key, newSimilarityMap.get(key)/maximum);
        }

        return newSimilarityMap;
    }

    public static HashMap<ObjectPair, Float> identityFloodingFunction(HashMap<ObjectPair, Float> defaultSimilarityMap, HashMap<ObjectPair, Float> similarityMap, PropagationGraph pGraph){
        return defaultSimilarityMap;
    }
}
