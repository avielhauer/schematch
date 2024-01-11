package de.uni_marburg.schematch.boosting.sf_algorithm.propagation_graph;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

@Getter
@RequiredArgsConstructor
public class PropagationNode{
    private final static Logger log = LogManager.getLogger(PropagationNode.class);
    final Object objectA;
    final Object objectB;

    private float initialSim;
    private float lastSim;
    private float sim;
    private float simCandidate;

    protected void setInitialSim(float initialSim){
        this.initialSim = initialSim;
        this.sim = initialSim;
        this.lastSim = -1F;
    }

    public void setSimCandidate(float simCandidate){
        this.simCandidate = simCandidate;
    }

    public void applyCandidate(){
        this.lastSim = this.sim;
        this.sim = this.simCandidate;
        this.simCandidate = -1F;
    }

    @Override
    public String toString(){
        return "["+this.objectA.toString()+","+this.objectB.toString()+"]";
    }

    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if (!(o instanceof PropagationNode)) return false;
        PropagationNode that = (PropagationNode) o;
        return this.getObjectA().equals(that.getObjectA()) && this.getObjectB().equals(that.getObjectB());
    }

    @Override
    public int hashCode(){
        return Objects.hash(objectA, objectB);
    }
}
