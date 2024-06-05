package de.uni_marburg.schematch.matching.sota.cupid;

import lombok.Getter;

import java.util.*;

public class SchemaTree {
    private Map<String, SchemaElementNode> nodes;
    private String schemaName;
    private SchemaElementNode schemaTree;

    @Getter
    private int hashCode;


    private SchemaElementNode rootNode;

    /**
     * Creates a Schema tree with root as its root
     * @param root root of the tree
     * @param hashCode hashcode of the Table object
     */
    public SchemaTree(SchemaElement root, int hashCode) {
        this.hashCode = hashCode;
        SchemaElement normalized = LinguisticMatching.normalization(root.getInitialName(), root);
        this.nodes = new HashMap<>();
        this.addRootNode(normalized);
        this.schemaName = normalized.getInitialName();
        this.schemaTree = null;
    }

    /**
     * Adds root node as root
     * @param root schema element which should be added as root
     */
    public void addRootNode(SchemaElement root) {
        this.rootNode = new SchemaElementNode(root.getInitialName(), null, new ArrayList<>(), root);
        nodes.put(root.getInitialName(), rootNode);
    }

    /**
     * Adds new node to the tree
     * @param name name of the schema element node
     * @param parent parent of the schema element node
     * @param children children of the schema element node
     * @param current schema element which should be added
     */
    public void addNode(String name, SchemaElementNode parent, ArrayList<SchemaElementNode> children, SchemaElement current) {
        SchemaElement normalized = LinguisticMatching.normalization(name, current);
        SchemaElementNode newNode = new SchemaElementNode(name, parent, children, normalized);
        //parent.children.add(newNode);
        nodes.put(name, newNode);
    }

    /**
     * @return List of leaves of the tree (List<SchemaElementNode>)
     */
    public List<SchemaElementNode> getLeaves() {
        List<SchemaElementNode> leaves = new ArrayList<>();
        for (SchemaElementNode element : this.nodes.values()) {
            if (element.getChildren().isEmpty()) {
                leaves.add(element);
            }
        }
        return leaves;
    }

    /**
     * @return height of the tree (int)
     */
    public int getHeight() {
        return getHeight(rootNode);
    }

    /**
     * Gets the specific height of the schema element node
     * @param node schema element node, of which the height should be calculated
     * @return height of the node
     */
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

    /**
     * @return root of the schema tree (SchemaElementNode)
     */
    public SchemaElementNode getRoot() {
        return rootNode;
    }

    /**
     * @return the schema trees nodes in post order (List<SchemaElementNode>)
     */
    public List<SchemaElementNode> postOrder() {
        return rootNode.postOrder();
    }
}
