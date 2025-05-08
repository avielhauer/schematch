package de.uni_marburg.schematch.boosting.sf_algorithm.flooding;

import de.uni_marburg.schematch.boosting.sf_algorithm.propagation_graph.PropagationGraph;
import de.uni_marburg.schematch.boosting.sf_algorithm.propagation_graph.PropagationNode;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.*;
import java.util.function.Consumer;

@RequiredArgsConstructor
public abstract class Flooder {
    private final static Logger log = LogManager.getLogger(Flooder.class);
    public final PropagationGraph<PropagationNode> pGraph;

    public final float[][] flood(int maxIterations, float minResidualLength){
        this.pGraph.resetNodeSimilarity();
        this.getMostConnectedPairs();
        for(int i = 0; i < maxIterations; i++){
            this.flooding_step();
            float sumSquaredDifference = 0;
            for(PropagationNode node: pGraph.vertexSet()){
                float difference = node.getSim()- node.getLastSim();
                sumSquaredDifference += difference * difference;
            }
            double residualLength = Math.sqrt(sumSquaredDifference);

            if(residualLength <= minResidualLength){
                log.debug("Flooding finished due to a residual vector of length {} after {} iterations.", residualLength, i);
                break;
            }
        }

        return pGraph.getSimMatrix();
    }

    private void getMostConnectedPairs(){
        // Berechne Degrees von jeder PropagationNode
        Collection listOfAllPropNodes = this.pGraph.getNodeList().values();
        Map<PropagationNode,Integer> nodeDegreeMap = new HashMap<>();
        for(Object pNode: listOfAllPropNodes){

            nodeDegreeMap.put((PropagationNode) pNode,this.pGraph.degreeOf((PropagationNode) pNode));
        }

        // Erstelle eine Liste aus den Einträgen der Map
        List<Map.Entry<PropagationNode, Integer>> entryList = new ArrayList<>(nodeDegreeMap.entrySet());

        // Sortiere die Liste anhand der Anzahl an Kanten
        entryList.sort(Map.Entry.comparingByValue());

        // Erstelle Sublist aus dem 75% Quantil
        entryList = entryList.subList(this.getIndexThirdQuartile(entryList), entryList.size());

        // TODO: Is this the right way of boosting pairs initially?
        Consumer<Map.Entry<PropagationNode, Integer>> addInitialBoost = value -> value.getKey().setInitialSim(0.5F);
        entryList.stream().forEach(addInitialBoost);
        entryList.get(0);
    }

    /**
     *
     * @param sortedList
     * @return index of third quartile of given list
     */
    private Integer getIndexThirdQuartile(List<Map.Entry<PropagationNode, Integer>> sortedList){
        int listSize = sortedList.size();
        Double p = 0.75;
        return (int) Math.ceil(p * listSize) - 1;
    }

    protected abstract void flooding_step();
}
