package de.uni_marburg.schematch.boosting.sf_algorithm.flooding;

import de.uni_marburg.schematch.boosting.sf_algorithm.propagation_graph.PropagationGraph;
import de.uni_marburg.schematch.boosting.sf_algorithm.propagation_graph.PropagationNode;
import de.uni_marburg.schematch.boosting.sf_algorithm.propagation_graph.WeightedEdge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FlooderA extends Flooder{
    private final static Logger log = LogManager.getLogger(FlooderA.class);
    public FlooderA(PropagationGraph<PropagationNode> pGraph) {
        super(pGraph);
    }

    @Override
    protected void flooding_step() {
        float max = 0F;
        for(PropagationNode node : this.pGraph.vertexSet()){
            node.setSimCandidate(node.getInitialSim());
            float simCandidate = node.getInitialSim();
            for(WeightedEdge edge : this.pGraph.incomingEdgesOf(node)){
                simCandidate += edge.getWeight() * this.pGraph.getEdgeTarget(edge).getSim();
            }
            node.setSimCandidate(simCandidate);
            max = Math.max(simCandidate, max);
        }
        for(PropagationNode node : this.pGraph.vertexSet()){
            node.setSimCandidate(node.getSimCandidate() / max);
            node.applyCandidate();
        }
    }
}
