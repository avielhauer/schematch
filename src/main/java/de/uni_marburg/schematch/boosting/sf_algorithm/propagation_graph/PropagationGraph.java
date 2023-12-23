package de.uni_marburg.schematch.boosting.sf_algorithm.propagation_graph;

import de.uni_marburg.schematch.boosting.sf_algorithm.db_2_graph.DBGraph;
import de.uni_marburg.schematch.boosting.sf_algorithm.db_2_graph.LabeledEdge;
import de.uni_marburg.schematch.boosting.sf_algorithm.similarity_calculator.SimilarityCalculator;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.graph.SimpleDirectedGraph;

import java.util.*;

@Getter
public abstract class PropagationGraph<T extends PropagationNode> extends SimpleDirectedGraph<T, WeightedEdge> {
    private final static Logger log = LogManager.getLogger(PropagationGraph.class);
    private final Map<T, T> nodeList;
    public final List<List<T>> columnNodes;

    public PropagationGraph(DBGraph dbGraphA, DBGraph dbGraphB, SimilarityCalculator similarityCalculator){
        super(WeightedEdge.class);
        this.nodeList = new HashMap<>();

        // Create ColumnNodes
        this.columnNodes = new ArrayList<>();
        for(int i = 0; i < dbGraphA.getColumns().size(); i++){
            List<T> myList = new ArrayList<>();
            this.columnNodes.add(myList);
            for(int j = 0; j < dbGraphB.getColumns().size(); j++){
                T node = this.createNode(dbGraphA.getColumns().get(i), dbGraphB.getColumns().get(j));
                myList.add(node);
                this.addVertex(node);
            }
        }

        Set<LabeledEdge> edgesA = dbGraphA.edgeSet();
        Set<LabeledEdge> edgesB = dbGraphB.edgeSet();

        for(LabeledEdge edgeA : edgesA){
            for(LabeledEdge edgeB : edgesB){
                if(edgeA.getLabel().equals(edgeB.getLabel())){
                    T pair1 = this.createNode(dbGraphA.getEdgeSource(edgeA), dbGraphB.getEdgeSource(edgeB));
                    T pair2 = this.createNode(dbGraphA.getEdgeTarget(edgeA), dbGraphB.getEdgeTarget(edgeB));
                    this.addVertex(pair1);
                    this.addVertex(pair2);
                    this.addEdge(pair1, pair2, new WeightedEdge(0));
                    this.addEdge(pair2, pair1, new WeightedEdge(0));
                }
            }
        }

        for(WeightedEdge edge: this.edgeSet()){
            this.generateEdgeWeight(edge);
        }
        this.generateNodeSimilarity(similarityCalculator);
        log.debug("Generated propagation graph with {} nodes and {} edges.", this.vertexSet().size(), this.edgeSet().size());
    }

    protected abstract T createNode(Object objectA, Object objectB);
    protected abstract void generateEdgeWeight(WeightedEdge edge);

    private void generateNodeSimilarity(SimilarityCalculator similarityCalculator){
        for(T node: this.vertexSet()){
            node.setInitialSim(similarityCalculator.calcSim(node));
        }
    }

    public final void resetNodeSimilarity(){
        for(T node: this.vertexSet()){
            node.setInitialSim(node.getInitialSim());
        }
    }

    public final float[][] getSimMatrix(){
        float[][] result = new float[this.columnNodes.size()][];
        for (int i = 0; i < this.columnNodes.size(); i++) {
            List<T> innerList = this.columnNodes.get(i);
            result[i] = new float[innerList.size()];
            for (int j = 0; j < innerList.size(); j++) {
                result[i][j] = innerList.get(j).getSim();
            }
        }
        return result;
    }

    @Override
    public boolean addVertex(T vertex){
        boolean result = super.addVertex(vertex);
        if(result){
            this.nodeList.put(vertex, vertex);
        }
        return result;
    }

    @Override
    public boolean addEdge(T nodeA, T nodeB, WeightedEdge edge){
        if(!this.nodeList.containsKey(nodeA) || !this.nodeList.containsKey(nodeB)){
            throw new RuntimeException("Edge vertices are not added.");
        }
        return(super.addEdge(this.nodeList.get(nodeA), this.nodeList.get(nodeB), edge));
    }
}
