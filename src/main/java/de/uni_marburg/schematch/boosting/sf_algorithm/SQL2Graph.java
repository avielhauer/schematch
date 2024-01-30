package de.uni_marburg.schematch.boosting.sf_algorithm;

import de.uni_marburg.schematch.data.Column;
import de.uni_marburg.schematch.data.Database;
import org.jgrapht.graph.SimpleDirectedGraph;
import de.uni_marburg.schematch.data.Table;

import java.awt.*;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
//Ok
public class SQL2Graph extends SimpleDirectedGraph<Object, LabeledEdge> {

    private final String DB_TYPE_NODE = "DB";
    private final String TABLE_TYPE_NODE = "Table";
    private final String COLUMN_TYPE_NODE= "Column";
    private final String COLUMN_TYPE_TYPE_NODE = "ColumnType";

    private Database database;

    private String graphName;

    private int unique_id;

    public SQL2Graph(Database database) {
        super(LabeledEdge.class);
        this.graphName = database.getName();
        this.database = database;
        this.unique_id = 0;
        this.addVertex(DB_TYPE_NODE);
        this.addVertex(TABLE_TYPE_NODE);
        this.addVertex(COLUMN_TYPE_NODE);
        this.addVertex(COLUMN_TYPE_TYPE_NODE);

        String DBname = database.getName();
        String dbVertex = this.createUniqueVertexLabel();
        String tableVertex;
        String tableName;
        String columnVertex;
        String columnName;
        String columnDataType;

        //Create Database Node
        this.addVertex(dbVertex);
        this.addVertex(DBname);
        this.addLabeledEdge(dbVertex, DBname, "name");
        this.addLabeledEdge(dbVertex, DB_TYPE_NODE, "type");
        // cretae table nodes and connect with DB-Node as its attribute "table"
        // TODO: Extract sequences to methods
        for (Table table:database.getTables().values()) {
            tableName = table.getName();
            tableVertex = createUniqueVertexLabel();
            this.addVertex(tableVertex);
            this.addVertex(tableName);
            this.addLabeledEdge(dbVertex,tableVertex, "table");
            this.addLabeledEdge(tableVertex, tableName, "name");
            this.addLabeledEdge(tableVertex, this.TABLE_TYPE_NODE, "type");

            //create column nodes and connect with corresponding table node as its attribute column
            for (Column column : table.getColumns()) {
                columnVertex = createUniqueVertexLabel();
                columnName = column.getLabel();
                this.addVertex(columnVertex);
                this.addVertex(columnName);
                this.addLabeledEdge(tableVertex, columnVertex, "column");
                this.addLabeledEdge(columnVertex, this.COLUMN_TYPE_NODE, "type");
                this.addLabeledEdge(columnVertex, columnName, "name");

                //create SQLType nodes and connect to column as its attribute SQLType
                columnDataType = column.getDatatype().name();
                String columnDataTypeVertex = createUniqueVertexLabel();
                this.addVertex(columnDataTypeVertex);
                this.addVertex(columnDataType);
                this.addLabeledEdge(columnVertex, columnDataTypeVertex, "SQLType");
                this.addLabeledEdge(columnDataTypeVertex,this.COLUMN_TYPE_TYPE_NODE, "type");
                this.addLabeledEdge(columnDataTypeVertex, columnDataType, "name");

            }
        }

        System.out.println(DBname);
    }

    private String createUniqueVertexLabel(){
        String uniqueVertexLabel = "VertexID_"+ unique_id+ "_"+graphName;
        unique_id += 1;
        return uniqueVertexLabel;
    }

    private void addLabeledEdge(Object source, Object target, String label){
        this.addEdge(source,target,new LabeledEdge(label));
    }

    public PropagationGraph generatePropagationGraph(SQL2Graph that){
        Set<LabeledEdge> thisEdges = this.edgeSet();
        Set<LabeledEdge> thatEdges = that.edgeSet();

        PropagationGraph pGraph = new PropagationGraph();

        for(LabeledEdge thisEdge : thisEdges){
            for(LabeledEdge thatEdge : thatEdges){
                if(Objects.equals(thisEdge.getLabel(), thatEdge.getLabel())){
                    ObjectPair pair1 = new ObjectPair(this.getEdgeSource(thisEdge), that.getEdgeSource(thatEdge));
                    ObjectPair pair2 = new ObjectPair(this.getEdgeTarget(thisEdge), that.getEdgeTarget(thatEdge));
                    pGraph.addVertex(pair1);
                    pGraph.addVertex(pair2);
                    pGraph.addEdge(pair1, pair2, new WeightedEdge(0));
                    pGraph.addEdge(pair2, pair1, new WeightedEdge(0));
                }
            }
        }
        // Set Edge weight, TODO: make this configurable
        for(ObjectPair vertex : pGraph.vertexSet()){
            Set<WeightedEdge> edges = pGraph.outgoingEdgesOf(vertex);
            for(WeightedEdge edge : edges){
                edge.setWeight((float) (1.0/edges.size()));
            }
        }
        return pGraph;
    }
}
