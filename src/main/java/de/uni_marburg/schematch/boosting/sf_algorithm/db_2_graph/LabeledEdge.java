package de.uni_marburg.schematch.boosting.sf_algorithm.db_2_graph;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@AllArgsConstructor
@Getter
public class LabeledEdge { // should NOT be converted to a record!
    private final static Logger log = LogManager.getLogger(LabeledEdge.class);
    private String label;
    @Override
    public String toString(){
        return "L:" + this.label;
    }
}
