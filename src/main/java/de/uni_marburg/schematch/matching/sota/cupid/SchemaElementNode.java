package de.uni_marburg.schematch.matching.sota.cupid;

import java.util.ArrayList;
import java.util.List;

public class SchemaElementNode{
    String name;
    SchemaElementNode parent;
    ArrayList<SchemaElementNode> children;

    public SchemaElementNode(String name, SchemaElementNode parent, ArrayList<SchemaElementNode> children) {
        this.name = name;
        this.parent = parent;
        this.children = children;

    }

    //noch implementieren
    public List<String> getLeafNames() {
        return null;
    }



}
