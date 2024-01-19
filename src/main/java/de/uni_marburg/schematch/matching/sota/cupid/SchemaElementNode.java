package de.uni_marburg.schematch.matching.sota.cupid;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
public class SchemaElementNode{

    String name;
    SchemaElementNode parent;
    @Getter
    ArrayList<SchemaElementNode> children;

    SchemaElement current;

    public SchemaElementNode(String name, SchemaElementNode parent, ArrayList<SchemaElementNode> children, SchemaElement current) {
        this.name = name;
        this.parent = parent;
        parent.getChildren().add(this);
        this.children = children;
        this.current = current;
    }


    public List<SchemaElementNode> postOrder() {
        ArrayList<SchemaElementNode> postOrderList = new ArrayList<SchemaElementNode>();
        if(!children.isEmpty()) children.forEach(schemaElementNode -> {
            if (!schemaElementNode.children.isEmpty()) {
                postOrderList.addAll(schemaElementNode.postOrder());
            } else {
                postOrderList.add(schemaElementNode);
            }
        });
        postOrderList.add(this);
        return postOrderList;
    }

    public int height() {
        if (children == null || children.isEmpty()) return 0;
        int height = 0;
        for (SchemaElementNode s: children) {
            int sHeight = s.height();
            height = Math.max(height,sHeight);
        }
        height++;
        return height;
    }

    public List<SchemaElementNode> leaves() {
        ArrayList<SchemaElementNode> leaves = new ArrayList<SchemaElementNode>();
        if (!children.isEmpty()) {
            for (SchemaElementNode s: children) {
                leaves.addAll(s.leaves());
            }
        } else {
            leaves.add(this);
        }
        return leaves;
    }
}
