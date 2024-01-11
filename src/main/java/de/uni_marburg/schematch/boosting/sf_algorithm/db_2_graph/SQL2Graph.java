package de.uni_marburg.schematch.boosting.sf_algorithm.db_2_graph;

import de.uni_marburg.schematch.data.Column;
import de.uni_marburg.schematch.data.Database;
import de.uni_marburg.schematch.data.Table;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class SQL2Graph extends DBGraph{
    private final static Logger log = LogManager.getLogger(SQL2Graph.class);

    public SQL2Graph(Database database) {
        super(database);
    }

    @Override
    protected void generateGraph(){
        // Add ColumnTypes
        this.addVertex("ColumnType");
        for(Column.Datatype type : Column.Datatype.values()){
            this.addVertex(type);
            this.addVertex(type.name());
            this.addEdge(type, "ColumnType", new LabeledEdge("type"));
            this.addEdge(type, type.name(), new LabeledEdge("name"));
        }

        // Add Tables
        this.addVertex("Table");
        List<Table> tables = this.getDatabase().getTables();
        for(Table table : tables){
            this.addVertex(table);
            this.addEdge(table, "Table", new LabeledEdge("type"));

            //Add Columns
            this.addVertex("Column");
            List<Column> columns = table.getColumns();
            for(Column column : columns){
                this.addVertex(column);
                this.addEdge(column, "Column", new LabeledEdge("type"));
                this.addEdge(table, column, new LabeledEdge("column"));
                this.addVertex(column.getLabel());
                this.addEdge(column, column.getLabel(), new LabeledEdge("name"));
                this.addEdge(column, column.getDatatype(), new LabeledEdge("SQLtype"));
                //TODO: Fetch Metadata from native metadata implementation (How does it work?)
            }
        }
    }
}
