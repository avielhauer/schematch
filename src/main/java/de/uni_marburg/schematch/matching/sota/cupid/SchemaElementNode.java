package de.uni_marburg.schematch.matching.sota.cupid;

import java.util.ArrayList;
import java.util.List;

public class SchemaElementNode{
    String name;
    SchemaElementNode parent;
    ArrayList<SchemaElementNode> children;

    SchemaElement current;

    public SchemaElementNode(String name, SchemaElementNode parent, ArrayList<SchemaElementNode> children, SchemaElement current) {
        this.name = name;
        this.parent = parent;
        this.children = children;
        this.current = current;
    }

    //noch implementieren
    public List<String> getLeafNames() {
        return null;
    }


    public SchemaElement getCurrent() {
        return current;
    }
}
