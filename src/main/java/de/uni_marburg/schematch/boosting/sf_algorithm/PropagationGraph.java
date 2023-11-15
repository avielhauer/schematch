package de.uni_marburg.schematch.boosting.sf_algorithm;

import de.uni_marburg.schematch.data.Column;
import de.uni_marburg.schematch.similarity.SimilarityMeasure;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.graph.SimpleDirectedGraph;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class PropagationGraph extends SimpleDirectedGraph<ObjectPair, WeightedEdge> {
    private final static Logger log = LogManager.getLogger(PropagationGraph.class);
    private Map<ObjectPair, Float> defaultSimilarityMap;
    private Map<ObjectPair, Float> similarityMap;
    private FloodingStep floodingFunction;

    public PropagationGraph() {
        super(WeightedEdge.class);
    }
    public void flood(int maxIterations, float minimumChange){
        if(defaultSimilarityMap == null){
            log.error("Error on sf: defaultSimilarity not set.");
            throw new RuntimeException("Error on sf: defaultSimilarity not set.");
        }
        if(floodingFunction == null){
            log.error("Error on sf: floodingFunction not set.");
            throw new RuntimeException("Error on sf: floodingFunction not set.");
        }
        similarityMap = new HashMap<>(defaultSimilarityMap);
        for(int i = 0; i < maxIterations; i++){
            Map<ObjectPair, Float> newSimilarityMap = floodingFunction.apply(defaultSimilarityMap, similarityMap, this);
            float maxChange = Float.MIN_VALUE;
            for(ObjectPair key : similarityMap.keySet()){
                float change = Math.abs(similarityMap.get(key) - newSimilarityMap.get(key));
                maxChange = Math.max(maxChange, change);
            }
            this.similarityMap = newSimilarityMap;
            if(maxChange <= minimumChange){
                log.debug("stopping because of minimumChange after " + (i+1) + "iterations.");
                break;
            }
        }

    }

    public Map<ObjectPair<Column, Column>, Float> getColumnSimilarity(){
        HashMap<ObjectPair<Column, Column>, Float> columnSimilarity = new HashMap<>();
        for(ObjectPair pair : this.vertexSet()){
            if(pair.objectA().getClass() == Column.class && pair.objectB().getClass() == Column.class){
                columnSimilarity.put(pair, this.similarityMap.get(pair));
            }
        }
        return columnSimilarity;
    }
    public void setDefaultSimilarityMap(SimilarityMeasure<String> stringSimilarity, Map<ObjectPair<Column, Column>, Float> columnSimilarity){
        this.defaultSimilarityMap = new HashMap<>();
        for(ObjectPair pair : this.vertexSet()){
            this.defaultSimilarityMap.put(pair, pair.similarity(stringSimilarity, columnSimilarity));
        }
    }

}
