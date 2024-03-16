package de.uni_marburg.schematch.matching.sota.cupid;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class SchemaElementNode{

    String name;
    SchemaElementNode parent;
    @Getter
    ArrayList<SchemaElementNode> children;

    SchemaElement current;

    /**
     * Initiates a schema element node with the given name, parent, children and the schema element
     * @param name name of the schema element node (String)
     * @param parent parent node of the schema element node (SchemaElementNode)
     * @param children list of the schema element nodes children (ArrayList<SchemaElementNode>)
     * @param current schema element which should be represented by the schema element node
     */
    public SchemaElementNode(String name, SchemaElementNode parent, ArrayList<SchemaElementNode> children, SchemaElement current) {
        this.name = name;
        this.parent = parent;
        if (parent != null)parent.getChildren().add(this);
        this.children = children;
        this.current = current;
    }

    /**
     * @return true if node is a leaf, false if node is no leaf
     */
    public boolean isLeave() {
        return children.isEmpty();
    }

    /**
     * @return list of schema element nodes in post order.
     */
    public List<SchemaElementNode> postOrder() {
        ArrayList<SchemaElementNode> postOrderList = new ArrayList<>();
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

    /**
     * @return height of the schema element node
     */
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

    /**
     * @return list of the schema elements leafs (List<SchemaElementNode>)
     */
    public List<SchemaElementNode> leaves() {
        ArrayList<SchemaElementNode> leaves = new ArrayList<>();
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
