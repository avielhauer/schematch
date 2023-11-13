package de.uni_marburg.schematch.boosting.sf_algorithm;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class LabeledEdge {
    private String label;

    @Override
    public String toString(){
        return "L:"+label;
    }
}
