package de.uni_marburg.schematch.boosting.sf_algorithm.db_2_graph;

import de.uni_marburg.schematch.boosting.sf_algorithm.propagation_graph.PropagationNode;
import de.uni_marburg.schematch.data.Column;
import de.uni_marburg.schematch.data.Database;
import de.uni_marburg.schematch.data.Table;
import de.uni_marburg.schematch.data.metadata.DatabaseMetadata;
import de.uni_marburg.schematch.data.metadata.Datatype;
import de.uni_marburg.schematch.data.metadata.PdepTuple;
import de.uni_marburg.schematch.data.metadata.dependency.FunctionalDependency;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class SQL2Graph extends DBGraph{
    private final static Logger log = LogManager.getLogger(SQL2Graph.class);

    public SQL2Graph(Database database) {
        super(database);
    }

    @Override
    protected void generateGraph(){

        Collection metadata = this.getDatabase().getMetadata().getFds();
        HashMap<FunctionalDependency, PdepTuple> map = new HashMap<>();
        Consumer<FunctionalDependency> checkGdepScore = value -> map.put(value, value.getPdepTuple());

        metadata.stream().forEach(checkGdepScore);
        //System.out.println(map);
        // Add Database Vertex
        Database database = this.getDatabase();

        /*
        this.addVertex("Database");
        this.addVertex(database.getName());
        this.addVertex(database);
        this.addEdge(database, "Database", new LabeledEdge("type"));
        this.addEdge(database, database.getName(), new LabeledEdge("name"));
         */
        // Add ColumnTypes
        this.addVertex("ColumnType");
        for(Datatype type : Datatype.values()){
            this.addVertex(type);
            this.addVertex(type.name());
            this.addEdge(type, "ColumnType", new LabeledEdge("type"));
            this.addEdge(type, type.name(), new LabeledEdge("name"));
        }

        // Add Tables
        this.addVertex("Table");
        List<Table> tables = database.getTables();
        for(Table table : tables){
            this.addVertex(table);
            this.addVertex(table.getName());

            // connect Database with Table
            //this.addEdge(database, table, new LabeledEdge("table"));
            this.addEdge(table, "Table", new LabeledEdge("type"));
            this.addEdge(table, table.getName(), new LabeledEdge("name"));
            //Add Columns
            this.addVertex("Column");
            List<Column> columns = table.getColumns();
            for(Column column : columns){
                this.addVertex(column);
                this.addVertex(column.getLabel());
                this.addEdge(table, column, new LabeledEdge("column"));

                this.addEdge(column, "Column", new LabeledEdge("type"));
                this.addEdge(column, column.getLabel(), new LabeledEdge("name"));
                this.addEdge(column, column.getDatatype(), new LabeledEdge("SQLtype"));
                //TODO: Fetch Metadata from native metadata implementation (How does it work?)
            }
        }
    }
}
