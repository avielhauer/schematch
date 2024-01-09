package de.uni_marburg.schematch.boosting.sf_algorithm;

import de.uni_marburg.schematch.data.Column;
import de.uni_marburg.schematch.data.Database;
import de.uni_marburg.schematch.data.Table;
import org.jgrapht.graph.SimpleDirectedGraph;

public class ValentineGraph extends SimpleDirectedGraph<Object, LabeledEdge> {

    private final Database schema;
    private Node databaseNode;
    private Node tableNode;
    private Node columnNode;
    private Node colTypeNode;
    private Node tblNode;
    private int uniqueId;

    public ValentineGraph(Database schema){
        super(LabeledEdge.class);
        this.schema = schema;

        this.databaseNode = new Node("DATABASE", this.schema.getName());
        this.tableNode = new Node("TABLE", this.schema.getName());
        this.columnNode = new Node("COLUMN", this.schema.getName());
        this.colTypeNode = new Node("COLUMN_TYPE", this.schema.getName());

        this.addVertex(this.tableNode);
        this.addVertex(this.columnNode);
        this.addVertex(this.colTypeNode);

        this.uniqueId = 1;
        this.tblNode = new Node("NodeID" + this.uniqueId, this.schema.getName());
        Node attributeNode = new Node(this.schema.getName(), this.schema.getName());
        this.addVertex(this.tblNode);
        this.addEdge(this.tblNode, attributeNode);
        this.addEdge(this.tblNode, this.tableNode);

        createGraph();
    }

    private Node createNode(Column column, String tableName, boolean typeNode, boolean attributeNode) {
        Node node;
        if (typeNode) {
            node = new Node(column.getDatatype().name(), this.schema.getName());
        } else if (attributeNode) {
            node = new Node(column.getDatatype().name(), this.schema.getName());
        } else {
            node = new Node("NodeID" + this.uniqueId, this.schema.getName());
        }
        return node;
    }

    public void addAndConnect(Column column, String tableName) {
        this.uniqueId += 1;
        Database table = this.schema;
        Node clmNode = createNode(column, tableName, false, false);
        Node attributeNode = createNode(column, tableName, false, true);
        this.addVertex(clmNode);
        this.addEdge(clmNode, this.columnNode);
        this.addEdge(this.tblNode, clmNode);
        this.addEdge(clmNode, attributeNode);

        Node typeNode = createNode(column, tableName, true, false);
        this.addVertex(typeNode);
        this.addEdge(clmNode, typeNode);

        if (this.containsVertex(typeNode)) {
            this.addEdge(clmNode, typeNode);
        } else {
            int previousId = this.uniqueId;
            this.uniqueId += 1;
            clmNode = createNode(column, tableName, false, false);
            this.addVertex(clmNode);
            this.addEdge(clmNode, this.colTypeNode);
            Node typeNameNode = createNode(column, tableName, true, true);
            this.addVertex(typeNameNode);
            this.addEdge(clmNode, typeNameNode);
            //this.addEdge(createNode(new Column("NodeID" + previousId), tableName, false, false), clmNode);
        }
    }

    public void createGraph() {
        for(Table table: this.schema.getTables().values())
            for (Column column : table.getColumns()) {
                addAndConnect(column, table.getName());
        }
    }
}