package de.uni_marburg.schematch.boosting.sf_algorithm;

import java.util.HashMap;
import java.util.Map;

public class FloodingFunctions {


    public static HashMap<ObjectPair, Float> identityFloodingFunction(HashMap<ObjectPair, Float> defaultSimilarityMap, HashMap<ObjectPair, Float> similarityMap, PropagationGraph pGraph){
        return defaultSimilarityMap;
    }


    public static HashMap<ObjectPair, Float> floodingFunctionBasic(
            Map<ObjectPair, Float> defaultSimilarityMap,
            Map<ObjectPair, Float> similarityMap,
            PropagationGraph pGraph
    ) {
        HashMap<ObjectPair, Float> newSimilarityMap = new HashMap<>(similarityMap);
        float maximum = Float.MIN_VALUE;

        for (ObjectPair target : pGraph.vertexSet()) {
            float sum = similarityMap.get(target);
            for (WeightedEdge edge : pGraph.outgoingEdgesOf(target)) {
                ObjectPair source = pGraph.getEdgeSource(edge);
                sum += edge.getWeight() * similarityMap.get(source);
            }

            float oldValue = newSimilarityMap.get(target);
            float bonus = sum;
            if (maximum < oldValue + bonus) maximum = oldValue + bonus;
            newSimilarityMap.put(target, oldValue + bonus);
        }

        for (ObjectPair key : newSimilarityMap.keySet()) {
            newSimilarityMap.put(key, newSimilarityMap.get(key) / maximum);
        }

        return newSimilarityMap;
    }



//    public static HashMap<ObjectPair, Float> floodingFunctionA(Map<ObjectPair, Float> defaultSimilarityMap, Map<ObjectPair, Float> similarityMap, PropagationGraph pGraph){
//        HashMap<ObjectPair, Float> newSimilarityMap = new HashMap<>(similarityMap);
//        float maximum = Float.MIN_VALUE;
//        for(WeightedEdge edge : pGraph.edgeSet()){
//            ObjectPair source = pGraph.getEdgeSource(edge);
//            ObjectPair target = pGraph.getEdgeTarget(edge);
//            float oldValue =  newSimilarityMap.get(target);
//            float bonus = edge.getWeight() * similarityMap.get(source);
//            if(maximum < oldValue + bonus) maximum = oldValue + bonus;
//            newSimilarityMap.put(target, oldValue + bonus);
//        }
//
//        for (ObjectPair key : newSimilarityMap.keySet()){
//            newSimilarityMap.put(key, newSimilarityMap.get(key)/maximum);
//        }
//
//        return newSimilarityMap;
//    }

    public static HashMap<ObjectPair, Float> floodingFunctionA(
            Map<ObjectPair, Float> defaultSimilarityMap,
            Map<ObjectPair, Float> similarityMap,
            PropagationGraph pGraph
    ) {
        HashMap<ObjectPair, Float> newSimilarityMap = new HashMap<>(similarityMap);
        float maximum = Float.MIN_VALUE;

        for (ObjectPair target : pGraph.vertexSet()) {
            float sum = defaultSimilarityMap.get(target);
            for (WeightedEdge edge : pGraph.outgoingEdgesOf(target)) {
                ObjectPair source = pGraph.getEdgeSource(edge);
                sum += edge.getWeight() * similarityMap.get(source);
            }

            float oldValue = newSimilarityMap.get(target);
            float bonus = sum;
            if (maximum < oldValue + bonus) maximum = oldValue + bonus;
            newSimilarityMap.put(target, oldValue + bonus);
        }

        for (ObjectPair key : newSimilarityMap.keySet()) {
            newSimilarityMap.put(key, newSimilarityMap.get(key) / maximum);
        }

        return newSimilarityMap;
    }


    public static HashMap<ObjectPair, Float> floodingFunctionB(
            Map<ObjectPair, Float> defaultSimilarityMap,
            Map<ObjectPair, Float> similarityMap,
            PropagationGraph pGraph
    ) {
        HashMap<ObjectPair, Float> newSimilarityMap = new HashMap<>(similarityMap);
        float maximum = Float.MIN_VALUE;

        for (ObjectPair target : pGraph.vertexSet()) {
            float sum = 0;
            for (WeightedEdge edge : pGraph.incomingEdgesOf(target)) {
                ObjectPair source = pGraph.getEdgeSource(edge);
                sum += edge.getWeight() * (defaultSimilarityMap.get(source) + similarityMap.get(source));
            }

            float oldValue = newSimilarityMap.get(target);
            float bonus = sum;
            if (maximum < oldValue + bonus) maximum = oldValue + bonus;
            newSimilarityMap.put(target, oldValue + bonus);
        }

        for (ObjectPair key : newSimilarityMap.keySet()) {
            newSimilarityMap.put(key, newSimilarityMap.get(key) / maximum);
        }

        return newSimilarityMap;
    }

    public static HashMap<ObjectPair, Float> floodingFunctionC(
            Map<ObjectPair, Float> defaultSimilarityMap,
            Map<ObjectPair, Float> similarityMap,
            PropagationGraph pGraph
    ) {
        HashMap<ObjectPair, Float> newSimilarityMap = new HashMap<>(similarityMap);
        float maximum = Float.MIN_VALUE;

        for (ObjectPair target : pGraph.vertexSet()) {
            float sum = defaultSimilarityMap.get(target);
            for (WeightedEdge edge : pGraph.outgoingEdgesOf(target)) {
                ObjectPair source = pGraph.getEdgeSource(edge);
                sum += edge.getWeight() * (defaultSimilarityMap.get(source) + similarityMap.get(source));
            }

            float oldValue = newSimilarityMap.get(target);
            float bonus = sum;
            if (maximum < oldValue + bonus) maximum = oldValue + bonus;
            newSimilarityMap.put(target, oldValue + bonus);
        }

        for (ObjectPair key : newSimilarityMap.keySet()) {
            newSimilarityMap.put(key, newSimilarityMap.get(key) / maximum);
        }

        return newSimilarityMap;
    }


}


/**
 *     Basic:
 *     si+1 = normalize(si+∑j(si))
 *
 *     A:
 *     si+1=normalize(s0+∑j(si))
 *
 *     B:
 *     si+1=normalize(∑j(s0+si))
 *
 *     C:*
 *     si+1=normalize(s0+si+∑j(s0+si))
 */
