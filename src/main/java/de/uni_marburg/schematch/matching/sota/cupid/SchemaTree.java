package de.uni_marburg.schematch.matching.sota.cupid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SchemaTree {
    private Map<String, SchemaElementNode> nodes;
    private String schemaName;
    private SchemaElementNode schemaTree;



    private SchemaElementNode rootNode;

    public SchemaTree(SchemaElement root) {
        this.nodes = new HashMap<>();
        this.addRootNode(root);
        this.schemaName = root.getInitialName();
        this.schemaTree = null;
    }

    public void addRootNode(SchemaElement root) {
        this.rootNode = new SchemaElementNode(root.getInitialName(), null, new ArrayList<>(), root);
    }

    public void addNode(String name, SchemaElementNode parent, ArrayList<SchemaElementNode> children, SchemaElement current) {
        SchemaElementNode newNode = new SchemaElementNode(name, parent, children, current);
        parent.children.add(newNode);
        nodes.put(name, newNode);
    }

   //public void printSchemaTree() {
   //    System.out.println(renderTree(nodes.get(schemaName), "", true));
   //}

    public List<SchemaElementNode> getLeaves() {
        List<SchemaElementNode> leaves = new ArrayList<>();
        for (SchemaElementNode element : this.nodes.values()) {
            if (element.getChildren().isEmpty()) {
                leaves.add(element);
            }
        }
        return leaves;
    }

    public List<String> getLeafNames() {
        List<String> leavesNames = new ArrayList<>();
        for (String element : this.nodes.keySet()) {
            if (nodes.get(element).getChildren().isEmpty()) {
                leavesNames.add(element);
            }
        }
        return leavesNames;
    }

    public int getHeight() {
        return getHeight(rootNode);
    }

    private int getHeight(SchemaElementNode node) {
        if (node == null) {
            return 0;
        }

        int maxHeight = 0;
        for (SchemaElementNode child : node.getChildren()) {
            int childHeight = getHeight(child);
            maxHeight = Math.max(maxHeight, childHeight);
        }

        return maxHeight + 1;
    }

    public SchemaElementNode getRoot() {
        return rootNode;
    }

    //private String renderTree(SchemaElementNode node) {
    //    return renderTree(node, "", false);
    //}

    //private String renderTree(SchemaElementNode node, String prefix, boolean isTail) {
    //   return null;
    //}

    public List<SchemaElementNode> postOrder() {
        return rootNode.postOrder();
    }
}
