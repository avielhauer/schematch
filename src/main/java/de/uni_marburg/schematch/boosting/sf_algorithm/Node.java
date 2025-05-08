package de.uni_marburg.schematch.boosting.sf_algorithm;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Node {
    private final String label;

    private final String schemaName;

    Node (String label, String schemaName){
        this.label = label;
        this.schemaName = schemaName;

    }


}
