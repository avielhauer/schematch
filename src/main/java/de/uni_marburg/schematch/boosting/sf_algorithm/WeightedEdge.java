package de.uni_marburg.schematch.boosting.sf_algorithm;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class WeightedEdge {
    private float weight;

    @Override
    public String toString(){
        return "W:"+weight;
    }
}

