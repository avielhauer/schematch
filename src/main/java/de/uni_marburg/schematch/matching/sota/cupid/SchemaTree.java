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

    public SchemaTree(String root) {
        this.nodes = new HashMap<>();
        this.addRootNode(root);
        this.schemaName = root;
        this.schemaTree = null;
    }

    public void addRootNode(String root) {
        this.rootNode = new SchemaElementNode(root, null, null);
    }

    public String getSchemaTree() {
        return renderTree(nodes.get(schemaName));
    }

    public SchemaElementNode getNode(String nodeName) {
        return nodes.get(nodeName);
    }

    public void addNode(String tableName, String tableGuid, String columnName, String columnGuid, String dataType, SchemaElementNode parent) {

    }

    public void printSchemaTree() {
        System.out.println(renderTree(nodes.get(schemaName), "", true));
    }

    public List<SchemaElementNode> getLeaves() {
        return null;
    }

    public List<String> getLeafNames() {
        return null;
    }

    public int getHeight() {
        return 0;
    }

    public SchemaElementNode getRoot() {
        return nodes.get(schemaName);
    }

    private String renderTree(SchemaElementNode node) {
        return renderTree(node, "", false);
    }

    private String renderTree(SchemaElementNode node, String prefix, boolean isTail) {
       return null;
    }
}
