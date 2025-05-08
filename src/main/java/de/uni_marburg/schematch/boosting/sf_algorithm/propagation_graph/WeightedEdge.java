package de.uni_marburg.schematch.boosting.sf_algorithm.propagation_graph;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@AllArgsConstructor
@Getter
@Setter
public class WeightedEdge {
    private final static Logger log = LogManager.getLogger(WeightedEdge.class);
    private float weight;

    @Override
    public String toString(){
        return "W:"+weight;
    }
}
