package de.uni_marburg.schematch.boosting.sf_algorithm.flooding;

import de.uni_marburg.schematch.boosting.sf_algorithm.propagation_graph.PropagationGraph;
import de.uni_marburg.schematch.boosting.sf_algorithm.propagation_graph.PropagationNode;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@RequiredArgsConstructor
public abstract class Flooder {
    private final static Logger log = LogManager.getLogger(Flooder.class);
    public final PropagationGraph<PropagationNode> pGraph;

    public final float[][] flood(int maxIterations, float minResidualLength){
        this.pGraph.resetNodeSimilarity();
        for(int i = 0; i < maxIterations; i++){
            this.flooding_step();
            float sumSquaredDifference = 0;
            for(PropagationNode node: pGraph.vertexSet()){
                float difference = node.getSim()- node.getLastSim();
                sumSquaredDifference += difference * difference;
            }
            double residualLength = Math.sqrt(sumSquaredDifference);

            if(residualLength <= minResidualLength){
                log.debug("Flooding finished due to a residual vector of length {}.", residualLength);
                break;
            }
        }

        return pGraph.getSimMatrix();
    }

    protected abstract void flooding_step();
}
